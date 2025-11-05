package com.sixpack.dorundorun.feature.friend.application;

import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.friend.exception.ReverseGeocodingErrorCode;
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

	public AddressInfo addressByCoordinates(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(latitude, longitude);
		}

		String cacheKey = CACHE_KEY_PREFIX + CoordinateUtil.roundToKey(latitude, longitude);

		String cachedJson = redisTemplate.opsForValue().get(cacheKey);
		// 캐시가 있으면 즉시 반환
		if (cachedJson != null) {
			return parseFromCacheSync(cachedJson);
		}

		// 캐시 없으면 API 호출
		int retryAttempts = (int)reverseGeocodingProperties.api().retryAttempts();
		long retryDelayMillis = reverseGeocodingProperties.api().retryDelayMillis();

		try {
			ReverseGeocodingResponse response = naverReverseGeocodingApi
				.reverseGeocode(latitude, longitude)
				.retryWhen(
					Retry.backoff(retryAttempts, Duration.ofMillis(retryDelayMillis))
						.filter(throwable ->
							throwable instanceof WebClientResponseException.ServiceUnavailable ||
								throwable instanceof WebClientResponseException.TooManyRequests ||
								throwable instanceof ClosedChannelException
						)
				)
				.block();  // 동기 대기

			AddressInfo addressInfo = toAddressInfo(response);
			cacheAddressInfo(cacheKey, addressInfo);
			return addressInfo;
		} catch (Exception e) {
			log.error("Reverse geocoding API 호출 실패: latitude={}, longitude={}, error={}",
				latitude, longitude, e.getMessage());
			throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(latitude, longitude);
		}
	}

	private AddressInfo toAddressInfo(ReverseGeocodingResponse response) {

		if (response == null || response.results() == null || response.results().isEmpty()) {
			return new AddressInfo(
				reverseGeocodingProperties.api().fallbackAddress(),
				LocalDateTime.now()
			);
		}

		ReverseGeocodingResponse.Region region = response.results().get(0).region();
		// 시/도 명 추출
		String area1 = region.area1() != null ? region.area1().name() : "";
		// 시/군/구 명 추출
		String area2 = region.area2() != null ? region.area2().name() : "";

		// "서울특별시" → "서울", "경기도" → "경기" 정제
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
			log.error("Redis 캐시 저장 실패: {}, error={}", cacheKey, e.getMessage());
			throw ReverseGeocodingErrorCode.CACHE_OPERATION_FAILED.format(cacheKey);
		}
	}

	private AddressInfo parseFromCacheSync(String json) {
		try {
			// JSON을 AddressInfo 객체로 역직렬화
			return objectMapper.readValue(json, AddressInfo.class);
		} catch (Exception e) {
			log.error("Redis 캐시 파싱 실패: {}, error={}", json, e.getMessage());
			throw ReverseGeocodingErrorCode.CACHE_OPERATION_FAILED.format(json);
		}
	}

}
