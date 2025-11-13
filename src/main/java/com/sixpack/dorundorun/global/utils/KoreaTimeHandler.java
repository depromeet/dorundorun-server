package com.sixpack.dorundorun.global.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 한국 시간대(Asia/Seoul) 기준으로 날짜/시간을 처리하는 핸들러
 * DB에 저장된 시간을 한국 시간대로 변환하거나, 한국 시간대 기준으로 현재 시간을 제공합니다.
 */
@Slf4j
@Component
public class KoreaTimeHandler {

	private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
	private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

	/**
	 * 한국 시간대 기준 현재 날짜를 반환합니다.
	 *
	 * @return 한국 시간대 기준 현재 날짜
	 */
	public LocalDate now() {
		return LocalDate.now(KOREA_ZONE);
	}

	/**
	 * 한국 시간대 기준 현재 날짜/시간을 반환합니다.
	 *
	 * @return 한국 시간대 기준 현재 날짜/시간
	 */
	public LocalDateTime nowDateTime() {
		return LocalDateTime.now(KOREA_ZONE);
	}

	/**
	 * UTC 시간을 한국 시간대로 변환합니다.
	 *
	 * @param utcDateTime UTC 시간
	 * @return 한국 시간대로 변환된 날짜/시간
	 */
	public LocalDateTime toKoreaTime(LocalDateTime utcDateTime) {
		if (utcDateTime == null) {
			return null;
		}
		LocalDateTime koreaTime = utcDateTime
			.atZone(UTC_ZONE)
			.withZoneSameInstant(KOREA_ZONE)
			.toLocalDateTime();
		log.debug("[KoreaTimeHandler] UTC {} -> 한국시간 {}", utcDateTime, koreaTime);
		return koreaTime;
	}

	/**
	 * UTC 시간을 한국 시간대의 날짜로 변환합니다.
	 *
	 * @param utcDateTime UTC 시간
	 * @return 한국 시간대로 변환된 날짜
	 */
	public LocalDate toKoreaDate(LocalDateTime utcDateTime) {
		if (utcDateTime == null) {
			return null;
		}
		return toKoreaTime(utcDateTime).toLocalDate();
	}

	/**
	 * 한국 시간대 기준 특정 날짜의 시작 시간(00:00:00)을 UTC로 반환합니다.
	 *
	 * @param date 한국 시간대 기준 날짜
	 * @return UTC 시간으로 변환된 해당 날짜의 시작 시간
	 */
	public LocalDateTime startOfDayInUtc(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.atStartOfDay(KOREA_ZONE)
			.withZoneSameInstant(ZoneId.of("UTC"))
			.toLocalDateTime();
	}

	/**
	 * 한국 시간대 기준 특정 날짜의 종료 시간(23:59:59.999999999)을 UTC로 반환합니다.
	 *
	 * @param date 한국 시간대 기준 날짜
	 * @return UTC 시간으로 변환된 해당 날짜의 종료 시간
	 */
	public LocalDateTime endOfDayInUtc(LocalDate date) {
		if (date == null) {
			return null;
		}
		return date.atTime(LocalTime.MAX)
			.atZone(KOREA_ZONE)
			.withZoneSameInstant(ZoneId.of("UTC"))
			.toLocalDateTime();
	}

	/**
	 * 두 시간이 한국 시간대 기준으로 같은 날인지 확인합니다.
	 *
	 * @param utcDateTime1 비교할 첫 번째 UTC 시간
	 * @param utcDateTime2 비교할 두 번째 UTC 시간
	 * @return 같은 날이면 true, 아니면 false
	 */
	public boolean isSameDayInKorea(LocalDateTime utcDateTime1, LocalDateTime utcDateTime2) {
		if (utcDateTime1 == null || utcDateTime2 == null) {
			return false;
		}
		return toKoreaDate(utcDateTime1).equals(toKoreaDate(utcDateTime2));
	}

	/**
	 * UTC 시간이 한국 시간대 기준으로 오늘인지 확인합니다.
	 *
	 * @param utcDateTime 확인할 UTC 시간
	 * @return 오늘이면 true, 아니면 false
	 */
	public boolean isTodayInKorea(LocalDateTime utcDateTime) {
		if (utcDateTime == null) {
			return false;
		}
		return toKoreaDate(utcDateTime).equals(now());
	}

	/**
	 * 한국 시간대 기준 오늘의 시작 시간(00:00:00)을 UTC로 반환합니다.
	 *
	 * @return UTC 시간으로 변환된 오늘의 시작 시간
	 */
	public LocalDateTime todayStartInUtc() {
		return startOfDayInUtc(now());
	}

	/**
	 * 한국 시간대 기준 오늘의 종료 시간(23:59:59.999999999)을 UTC로 반환합니다.
	 *
	 * @return UTC 시간으로 변환된 오늘의 종료 시간
	 */
	public LocalDateTime todayEndInUtc() {
		return endOfDayInUtc(now());
	}
}
