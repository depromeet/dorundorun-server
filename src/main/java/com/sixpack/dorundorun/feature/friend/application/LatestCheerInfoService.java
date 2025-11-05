package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LatestCheerInfoService {

	private final NotificationJpaRepository notificationJpaRepository;

	public Map<Long, LocalDateTime> getLatestCheerMap(Long userId, List<Long> friendIds) {

		Map<Long, LocalDateTime> latestCheer = new HashMap<>();
		if (friendIds.isEmpty()) {
			return latestCheer;
		}

		List<Map<String, Object>> cheerResults = notificationJpaRepository.findLatestCheersByUserAndFriends(userId,
			friendIds);

		for (Map<String, Object> result : cheerResults) {

			Long friendId = ((Number)result.get("friendId")).longValue();
			Object dateObj = result.get("latestCheeredAt");
			LocalDateTime latestCheeredAt = null;
			if (dateObj instanceof java.sql.Timestamp) {
				latestCheeredAt = ((java.sql.Timestamp)dateObj).toLocalDateTime();
			} else if (dateObj instanceof LocalDateTime) {
				latestCheeredAt = (LocalDateTime)dateObj;
			}

			latestCheer.put(friendId, latestCheeredAt);
		}

		return latestCheer;
	}
}
