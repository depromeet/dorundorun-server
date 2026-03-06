package com.sixpack.dorundorun.feature.friend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.utils.CoordinateUtil;
import com.sixpack.dorundorun.infra.naver.api.NaverReverseGeocodingApi;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;
import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

    private final StringRedisTemplate redisTemplate;
    private final NaverReverseGeocodingApi naverReverseGeocodingApi;
    private final ObjectMapper objectMapper;
    private final ReverseGeocodingProperties reverseGeocodingProperties;

    private static final String CACHE_KEY_PREFIX = "reverse-geocoding:";

    public CompletableFuture<AddressInfo> addressByCoordinatesAsync(
            Double latitude, Double longitude, Executor executor) {

        String cacheKey = CACHE_KEY_PREFIX + CoordinateUtil.roundToKey(latitude, longitude);

        return CompletableFuture.supplyAsync(() -> {
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
        }, executor);
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
