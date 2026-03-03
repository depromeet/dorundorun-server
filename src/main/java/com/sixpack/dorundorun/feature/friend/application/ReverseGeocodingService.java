package com.sixpack.dorundorun.feature.friend.application;

import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.global.config.webclient.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.utils.CoordinateUtil;
import com.sixpack.dorundorun.infra.naver.api.NaverReverseGeocodingApi;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;
import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

	private final StringRedisTemplate redisTemplate;
	private final NaverReverseGeocodingApi naverReverseGeocodingApi;
	private final ObjectMapper objectMapper;
	private final ReverseGeocodingProperties reverseGeocodingProperties;

	private static final String CACHE_KEY_PREFIX = "reverse-geocoding:";

	/**
	 * 비동기 좌표→주소 변환.
	 * Redis 캐시 조회(blocking)는 Executor에서 실행하고,
	 * 캐시 미스 시 WebClient Mono를 .toFuture()로 변환하여 논블로킹 API 호출 수행.
	 */
	public CompletableFuture<AddressInfo> addressByCoordinatesAsync(
		Double latitude, Double longitude, Executor executor) {

		String cacheKey = CACHE_KEY_PREFIX + CoordinateUtil.roundToKey(latitude, longitude);

		// 1단계: Redis 캐시 조회 (blocking I/O → Executor 스레드에서 실행)
		return CompletableFuture.supplyAsync(() -> {
			try {
				String cachedJson = redisTemplate.opsForValue().get(cacheKey);
				if (cachedJson != null) {
					return parseFromCache(cachedJson);
				}
			} catch (Exception e) {
				log.warn("Redis 캐시 조회 실패, API 호출로 대체: key={}, error={}", cacheKey, e.getMessage());
			}
			return null;
		}, executor).thenCompose(cached -> {
			// 2단계: 캐시 히트 시 즉시 반환
			if (cached != null) {
				return CompletableFuture.completedFuture(cached);
			}

			// 3단계: 캐시 미스 → WebClient Mono.toFuture() (논블로킹)
			int retryAttempts = (int)reverseGeocodingProperties.api().retryAttempts();
			long retryDelayMillis = reverseGeocodingProperties.api().retryDelayMillis();

			return naverReverseGeocodingApi
				.reverseGeocode(latitude, longitude)
				.retryWhen(
					Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMillis))
						.filter(throwable ->
							throwable instanceof WebClientResponseException.ServiceUnavailable ||
								throwable instanceof WebClientResponseException.TooManyRequests ||
								throwable instanceof ClosedChannelException
						)
				)
				.map(this::toAddressInfo)
				.doOnNext(addressInfo -> cacheAddressInfo(cacheKey, addressInfo))
				.toFuture();
		});
	}

	private AddressInfo toAddressInfo(ReverseGeocodingResponse response) {

		if (response == null || response.results() == null || response.results().isEmpty()) {
			return new AddressInfo(
				reverseGeocodingProperties.api().fallbackAddress(),
				LocalDateTime.now()
			);
		}

		ReverseGeocodingResponse.Region region = response.results().get(0).region();
		String area1 = region.area1() != null ? region.area1().name() : "";
		String area2 = region.area2() != null ? region.area2().name() : "";

		area1 = cleanAreaName(area1);

		String address = (area1 + " " + area2).trim();

		return new AddressInfo(address, LocalDateTime.now());
	}

	private String cleanAreaName(String area) {
		if (area == null || area.isEmpty()) {
			return area;
		}
		return area
			.replace("특별시", "")
			.replace("광역시", "")
			.replace("도", "")
			.trim();
	}

	private void cacheAddressInfo(String cacheKey, AddressInfo addressInfo) {
		try {
			String json = objectMapper.writeValueAsString(addressInfo);
			long ttlHours = reverseGeocodingProperties.cache().ttlHours();
			redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(ttlHours));
		} catch (Exception e) {
			log.warn("Redis 캐시 저장 실패 (무시): key={}, error={}", cacheKey, e.getMessage());
		}
	}

	private AddressInfo parseFromCache(String json) {
		try {
			return objectMapper.readValue(json, AddressInfo.class);
		} catch (Exception e) {
			log.warn("Redis 캐시 파싱 실패, API 호출로 대체: error={}", e.getMessage());
			return null;
		}
	}

}
