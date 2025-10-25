package com.sixpack.dorundorun.feature.feed.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sixpack.dorundorun.feature.feed.dto.projection.FeedCountByDateProjection;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;

public record DailyCertifications(
	Map<LocalDate, Long> dateCountMap
) {

	public static DailyCertifications from(List<FeedCountByDateProjection> queryResults) {
		Map<LocalDate, Long> dateCountMap = queryResults.stream()
			.collect(Collectors.toMap(
				FeedCountByDateProjection::getDate,
				FeedCountByDateProjection::getCount
			));
		return new DailyCertifications(dateCountMap);
	}

	public List<SelfieWeekResponse.DailyCertification> toResponseList(LocalDate startDate, LocalDate endDate) {
		List<SelfieWeekResponse.DailyCertification> data = new ArrayList<>();
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			Long count = dateCountMap.getOrDefault(currentDate, 0L);
			data.add(new SelfieWeekResponse.DailyCertification(
				currentDate.toString(),
				count.intValue()
			));
			currentDate = currentDate.plusDays(1);
		}

		return data;
	}

	public static List<SelfieWeekResponse.DailyCertification> createEmptyList(LocalDate startDate, LocalDate endDate) {
		List<SelfieWeekResponse.DailyCertification> data = new ArrayList<>();
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			data.add(new SelfieWeekResponse.DailyCertification(
				currentDate.toString(),
				0
			));
			currentDate = currentDate.plusDays(1);
		}

		return data;
	}
}