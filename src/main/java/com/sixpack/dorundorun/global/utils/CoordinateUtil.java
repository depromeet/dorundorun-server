package com.sixpack.dorundorun.global.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoordinateUtil {

	private static final int DECIMAL_PLACES = 3;
	private static final String COORDINATE_SEPARATOR = ":";

	public static String roundToKey(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			throw new NullPointerException("latitude and longitude must not be null");
		}

		String roundedLatitude = roundToString(latitude);
		String roundedLongitude = roundToString(longitude);

		return roundedLatitude + COORDINATE_SEPARATOR + roundedLongitude;
	}

	public static String roundToKey(RunSegmentData segmentData) {
		if (segmentData == null) {
			throw new NullPointerException("segmentData must not be null");
		}

		return roundToKey(segmentData.latitude(), segmentData.longitude());
	}

	private static String roundToString(Double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}
}
