package com.sixpack.dorundorun.feature.friend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.utils.CoordinateUtil;
import com.sixpack.dorundorun.infra.naver.api.NaverReverseGeocodingApi;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;
import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ReverseGeocodingService {

	private static final String CACHE_KEY_PREFIX = "reverse-geocoding:";
	private static final long LOCAL_CACHE_MAX_SIZE = 10_000;
	private static final double TTL_JITTER_RATIO = 0.1;

	private final StringRedisTemplate redisTemplate;
	private final NaverReverseGeocodingApi naverReverseGeocodingApi;
	private final ObjectMapper objectMapper;
	private final ReverseGeocodingProperties reverseGeocodingProperties;

	// 동일 좌표에 대한 동시 요청을 하나의 계산으로 합쳐 캐시 스탬피드를 방지하는 in-process single-flight 캐시
	private final AsyncCache<String, AddressInfo> localCache;

	public ReverseGeocodingService(
			StringRedisTemplate redisTemplate,
			NaverReverseGeocodingApi naverReverseGeocodingApi,
			ObjectMapper objectMapper,
			ReverseGeocodingProperties reverseGeocodingProperties) {
		this.redisTemplate = redisTemplate;
		this.naverReverseGeocodingApi = naverReverseGeocodingApi;
		this.objectMapper = objectMapper;
		this.reverseGeocodingProperties = reverseGeocodingProperties;
		this.localCache = Caffeine.newBuilder()
				// 로딩 작업(Redis 조회, Naver API 호출, 재시도 sleep)이 블로킹 I/O이므로
				// JVM 공용 ForkJoinPool.commonPool() 대신 호출 스레드에서 직접 실행
				.executor(Runnable::run)
				.maximumSize(LOCAL_CACHE_MAX_SIZE)
				.expireAfterWrite(Duration.ofHours(reverseGeocodingProperties.cache().ttlHours()))
				.buildAsync();
	}

	public CompletableFuture<AddressInfo> addressByCoordinatesAsync(Double latitude, Double longitude) {

		String cacheKey = CACHE_KEY_PREFIX + CoordinateUtil.roundToKey(latitude, longitude);

		// 같은 키로 동시에 들어온 요청은 진행 중인 future에 합류하며, 로딩 함수는 키당 한 번만 실행됨
		return localCache.get(cacheKey,
				(key, executor) -> CompletableFuture.supplyAsync(
						() -> loadAddress(key, latitude, longitude), executor));
	}

	private AddressInfo loadAddress(String cacheKey, Double latitude, Double longitude) {
		// 1단계: Redis 캐시 조회
		try {
			String cachedJson = redisTemplate.opsForValue().get(cacheKey);
			if (cachedJson != null) {
				AddressInfo cached = parseFromCache(cachedJson);
				if (cached != null) {
					return cached;
				}
			}
		} catch (Exception e) {
			log.warn("Redis 캐시 조회 실패, API 호출로 대체: key={}, error={}", cacheKey, e.getMessage());
		}

		// 2단계: 캐시 미스 → API 호출 (retry 포함)
		AddressInfo result = callWithRetry(latitude, longitude);
		cacheAddressInfo(cacheKey, result);
		return result;
	}

    private AddressInfo callWithRetry(Double latitude, Double longitude) {
        int retryAttempts = reverseGeocodingProperties.api().retryAttempts();
        long retryDelayMillis = reverseGeocodingProperties.api().retryDelayMillis();

        for (int attempt = 0; attempt <= retryAttempts; attempt++) {
            try {
                ReverseGeocodingResponse response = naverReverseGeocodingApi.reverseGeocode(latitude, longitude);
                return toAddressInfo(response);
            } catch (HttpServerErrorException.ServiceUnavailable |
                     HttpClientErrorException.TooManyRequests |
                     ResourceAccessException e) {
                if (attempt == retryAttempts) {
                    log.warn("Reverse geocoding 재시도 초과: lat={}, lon={}, error={}", latitude, longitude,
                            e.getMessage());
                    break;
                }
                log.warn("Reverse geocoding 재시도 {}/{}: lat={}, lon={}, error={}",
                        attempt + 1, retryAttempts, latitude, longitude, e.getMessage());
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                log.warn("Reverse geocoding API 호출 실패: lat={}, lon={}, error={}", latitude, longitude,
                        e.getMessage());
                break;
            }
        }

        return toAddressInfo(null);
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
			redisTemplate.opsForValue().set(cacheKey, json, jitteredTtl());
		} catch (Exception e) {
			log.warn("Redis 캐시 저장 실패 (무시): key={}, error={}", cacheKey, e.getMessage());
		}
	}

	// 같은 시점에 채워진 캐시 키들이 한꺼번에 만료되어 API 호출이 몰리는 것을 방지하기 위한 TTL 지터(±10%)
	private Duration jitteredTtl() {
		long ttlSeconds = Duration.ofHours(reverseGeocodingProperties.cache().ttlHours()).toSeconds();
		long jitterRangeSeconds = (long) (ttlSeconds * TTL_JITTER_RATIO);
		long jitterSeconds = ThreadLocalRandom.current().nextLong(-jitterRangeSeconds, jitterRangeSeconds + 1);
		return Duration.ofSeconds(ttlSeconds + jitterSeconds);
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
