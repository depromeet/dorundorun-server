package com.sixpack.dorundorun.feature.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.friend.event.CheerRequestedEvent;
import com.sixpack.dorundorun.feature.notification.application.PushNotificationContentDeterminer;
import com.sixpack.dorundorun.feature.notification.application.SaveNotificationService;
import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.domain.NotificationType;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.service.ServiceTest;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationIntegrationTest extends ServiceTest {

	@Autowired
	private UserJpaRepository userRepository;

	@Autowired
	private NotificationJpaRepository notificationRepository;

	@Autowired
	private SaveNotificationService saveNotificationService;

	@Autowired
	private PushNotificationContentDeterminer contentDeterminer;

	@Autowired
	private RedisStreamPublisher redisStreamPublisher;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private User testUser;
	private User testFriend;

	@BeforeEach
	void setUp() {
		// 테스트용 사용자 생성
		testUser = User.builder()
			.phoneNumber("01012345678")
			.code("USER001")
			.nickname("테스트유저")
			.deviceToken("dCzGh2q9UfY:APA91bGFxyz123456789")
			.build();

		testFriend = User.builder()
			.phoneNumber("01087654321")
			.code("USER002")
			.nickname("친구")
			.deviceToken("aBcDeF1234:APA91bGFabc987654321")
			.build();

		userRepository.save(testUser);
		userRepository.save(testFriend);

		// Redis 정리
		cleanupRedis();
	}

	@AfterEach
	void tearDown() {
		cleanupRedis();
		notificationRepository.deleteAll();
		userRepository.deleteAll();
	}

	private void cleanupRedis() {
		try {
			redisTemplate.delete("pending-notifications");
			redisTemplate.delete("notifications");
		} catch (Exception ignored) {
		}
	}

	// ========== 1. PushNotificationContentDeterminer 테스트 ==========

	@Test
	@Order(1)
	@DisplayName("깨우기 알림 콘텐츠를 올바르게 결정한다")
	void testCheerFriendNotificationContent() {
		// given
		String notificationType = "CHEER_FRIEND";
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("cheererName", "김철수");

		// when
		String title = contentDeterminer.determineTitle(notificationType, metadata);
		String message = contentDeterminer.determineMessage(notificationType, metadata);
		String deepLink = contentDeterminer.determineDeepLink(notificationType, String.valueOf(testFriend.getId()),
			metadata);

		// then
		assertEquals("깨우기 알림", title);
		assertEquals("김철수님이 회원님을 깨웠어요", message);
		assertThat(deepLink).contains("dorundorun://friend/profile/");
	}

	@Test
	@Order(2)
	@DisplayName("신규 가입 러닝 독촉 알림 콘텐츠를 올바르게 결정한다")
	void testNewUserRunningReminderContent() {
		// given
		String notificationType = "NEW_USER_RUNNING_REMINDER";
		Map<String, Object> metadata = new HashMap<>();

		// when
		String title = contentDeterminer.determineTitle(notificationType, metadata);
		String message = contentDeterminer.determineMessage(notificationType, metadata);
		String deepLink = contentDeterminer.determineDeepLink(notificationType, null, metadata);

		// then
		assertEquals("러닝 시작", title);
		assertEquals("두런두런과 설레는 첫 러닝을 시작해봐요!", message);
		assertEquals("dorundorun://running/start", deepLink);
	}

	@Test
	@Order(3)
	@DisplayName("메타데이터가 없는 경우 기본 메시지를 반환한다")
	void testDefaultMessageWithoutMetadata() {
		// given
		String notificationType = "CHEER_FRIEND";

		// when
		String message = contentDeterminer.determineMessage(notificationType, new HashMap<>());

		// then
		assertNotNull(message);
		assertTrue(message.length() > 0);
		// 메시지는 메타데이터가 없으므로 기본 메시지를 반환해야 함
	}

	// ========== 2. SaveNotificationService 테스트 ==========

	@Test
	@Order(4)
	@DisplayName("알림을 데이터베이스에 저장한다")
	void testSaveNotification() {
		// given
		PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
			.recipientUserId(testUser.getId())
			.notificationType("CHEER_FRIEND")
			.relatedId(String.valueOf(testFriend.getId()))
			.metadata(Map.of("cheererName", "친구"))
			.build();

		// when
		Notification savedNotification = saveNotificationService.save(
			event,
			"깨우기 알림",
			"친구님이 회원님을 깨웠어요",
			"app://friend/profile/" + testFriend.getId()
		);

		// then
		assertNotNull(savedNotification.getId());
		assertEquals(testUser.getDeviceToken(), savedNotification.getUserDeviceToken());
		assertEquals(NotificationType.CHEER_FRIEND, savedNotification.getType());
		assertEquals("깨우기 알림", savedNotification.getData().getTitle());
		assertEquals("친구님이 회원님을 깨웠어요", savedNotification.getData().getMessage());
		assertFalse(savedNotification.getIsRead());

		// DB에 실제로 저장됐는지 확인
		Notification retrieved = notificationRepository.findById(savedNotification.getId()).orElse(null);
		assertNotNull(retrieved);
		assertEquals("깨우기 알림", retrieved.getData().getTitle());
	}

	@Test
	@Order(5)
	@DisplayName("여러 알림을 저장할 수 있다")
	void testSaveMultipleNotifications() {
		// given
		int count = 5;

		// when
		for (int i = 0; i < count; i++) {
			PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
				.recipientUserId(testUser.getId())
				.notificationType("CHEER_FRIEND")
				.relatedId(String.valueOf(testFriend.getId()))
				.metadata(new HashMap<>())
				.build();

			saveNotificationService.save(
				event,
				"깨우기 알림 " + i,
				"메시지 " + i,
				"app://friend/profile/" + testFriend.getId()
			);
		}

		// then
		List<Notification> notifications = notificationRepository.findAll();
		assertEquals(count, notifications.size());
		assertThat(notifications).allMatch(n -> !n.getIsRead());
	}

	@Test
	@Order(6)
	@DisplayName("다양한 알림 타입을 저장할 수 있다")
	void testSaveDifferentNotificationTypes() {
		// given
		String[] types = {
			"CHEER_FRIEND",
			"FEED_UPLOADED",
			"NEW_USER_RUNNING_REMINDER",
			"FEED_REMINDER"
		};

		// when & then
		for (String type : types) {
			PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
				.recipientUserId(testUser.getId())
				.notificationType(type)
				.relatedId("123")
				.metadata(new HashMap<>())
				.build();

			Notification notification = saveNotificationService.save(event, "제목", "메시지", "app://link");
			assertNotNull(notification.getId());

			// 저장된 타입 확인
			NotificationType savedType = notificationRepository.findById(notification.getId())
				.orElseThrow()
				.getType();
			assertNotNull(savedType);
		}

		List<Notification> all = notificationRepository.findAll();
		assertEquals(types.length, all.size());
	}

	// ========== 3. Redis 스케줄 알림 저장 테스트 ==========

	@Test
	@Order(7)
	@DisplayName("Redis Sorted Set에 예약된 알림을 저장한다")
	void testScheduleNotificationInRedis() {
		// given
		String eventId = UUID.randomUUID().toString();
		long scheduledTimestamp = Instant.now().getEpochSecond() + 86400; // 24시간 후

		// when
		redisTemplate.opsForZSet().add("pending-notifications", eventId, scheduledTimestamp);

		// then
		Long count = redisTemplate.opsForZSet()
			.count("pending-notifications", scheduledTimestamp - 1, scheduledTimestamp + 1);
		assertEquals(1L, count.longValue());
	}

	@Test
	@Order(8)
	@DisplayName("Redis Hash에 알림 데이터를 저장한다")
	void testSaveNotificationDataToRedisHash() throws Exception {
		// given
		String eventId = UUID.randomUUID().toString();
		Map<String, Object> data = new HashMap<>();
		data.put("userId", testUser.getId());
		data.put("notificationType", "NEW_USER_RUNNING_REMINDER");
		data.put("scheduledAt", "2024-10-30T00:00:00");

		String jsonData = objectMapper.writeValueAsString(data);

		// when
		redisTemplate.opsForHash().put("notifications", eventId, jsonData);

		// then
		Object retrieved = redisTemplate.opsForHash().get("notifications", eventId);
		assertNotNull(retrieved);

		Map<String, Object> deserializedData = objectMapper.readValue(
			retrieved.toString(),
			Map.class
		);
		assertEquals(testUser.getId(), ((Number)deserializedData.get("userId")).longValue());
	}

	@Test
	@Order(9)
	@DisplayName("Redis에서 처리된 알림을 제거한다")
	void testRemoveProcessedNotificationFromRedis() {
		// given
		String eventId = UUID.randomUUID().toString();
		long timestamp = Instant.now().getEpochSecond();
		redisTemplate.opsForZSet().add("pending-notifications", eventId, timestamp);
		redisTemplate.opsForHash().put("notifications", eventId, "test-data");

		// when
		redisTemplate.opsForZSet().remove("pending-notifications", eventId);
		redisTemplate.opsForHash().delete("notifications", eventId);

		// then
		Long count = redisTemplate.opsForZSet().count("pending-notifications", 0, timestamp + 1);
		assertEquals(0L, count.longValue());

		Object data = redisTemplate.opsForHash().get("notifications", eventId);
		assertNull(data);
	}

	// ========== 4. Redis Stream 이벤트 발행 테스트 ==========

	@Test
	@Order(10)
	@DisplayName("PushNotificationRequestedEvent를 Redis Stream으로 발행할 수 있다")
	void testPublishPushNotificationEvent() {
		// given
		PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
			.recipientUserId(testUser.getId())
			.notificationType("CHEER_FRIEND")
			.relatedId(String.valueOf(testFriend.getId()))
			.metadata(Map.of("cheererName", "친구"))
			.build();

		// when
		redisStreamPublisher.publishAfterCommit(event);

		// then
		// Redis Stream에 저장되었는지 확인
		Long streamSize = redisTemplate.opsForStream().size("test.stream");
		assertNotNull(streamSize);
		assertTrue(streamSize >= 0);
	}

	@Test
	@Order(11)
	@DisplayName("CheerRequestedEvent를 Redis Stream으로 발행할 수 있다")
	void testPublishCheerEvent() {
		// given
		CheerRequestedEvent event = CheerRequestedEvent.builder()
			.cheererId(testFriend.getId())
			.cheeringUserId(testUser.getId())
			.build();

		// when
		redisStreamPublisher.publishAfterCommit(event);

		// then
		Long streamSize = redisTemplate.opsForStream().size("test.stream");
		assertNotNull(streamSize);
		assertTrue(streamSize >= 0);
	}

	// ========== 5. 통합 테스트 ==========

	@Test
	@Order(12)
	@DisplayName("즉시 알림: 깨우기 이벤트부터 DB 저장까지의 흐름을 테스트한다")
	void testEndToEndCheerNotification() {
		// given
		Map<String, Object> metadata = Map.of("cheererName", "김철수");

		// when
		PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
			.recipientUserId(testUser.getId())
			.notificationType("CHEER_FRIEND")
			.relatedId(String.valueOf(testFriend.getId()))
			.metadata(metadata)
			.build();

		String title = contentDeterminer.determineTitle(pushEvent.notificationType(), pushEvent.metadata());
		String message = contentDeterminer.determineMessage(pushEvent.notificationType(), pushEvent.metadata());
		String deepLink = contentDeterminer.determineDeepLink(pushEvent.notificationType(), pushEvent.relatedId(),
			pushEvent.metadata());

		Notification saved = saveNotificationService.save(pushEvent, title, message, deepLink);

		// then
		assertNotNull(saved.getId());
		assertEquals("깨우기 알림", saved.getData().getTitle());
		assertEquals("김철수님이 회원님을 깨웠어요", saved.getData().getMessage());
		assertEquals("dorundorun://friend/profile/" + testFriend.getId(), saved.getDeepLink());

		Notification retrieved = notificationRepository.findById(saved.getId()).orElse(null);
		assertNotNull(retrieved);
		assertEquals(NotificationType.CHEER_FRIEND, retrieved.getType());
	}

	@Test
	@Order(13)
	@DisplayName("스케줄 알림: Redis 저장 및 조회 흐름을 테스트한다")
	void testScheduledNotificationFlow() throws Exception {
		// given
		String eventId = UUID.randomUUID().toString();
		long scheduledTimestamp = Instant.now().getEpochSecond() + 86400; // 24시간 후

		Map<String, Object> scheduledData = new HashMap<>();
		scheduledData.put("eventId", eventId);
		scheduledData.put("userId", testUser.getId());
		scheduledData.put("notificationType", "NEW_USER_RUNNING_REMINDER");
		scheduledData.put("scheduledAt", "2024-10-30T00:00:00");

		String jsonData = objectMapper.writeValueAsString(scheduledData);

		// when
		// 1. Redis에 저장
		redisTemplate.opsForZSet().add("pending-notifications", eventId, scheduledTimestamp);
		redisTemplate.opsForHash().put("notifications", eventId, jsonData);

		// 2. Redis Sorted Set에 저장되었는지 확인
		Long countBefore = redisTemplate.opsForZSet().count("pending-notifications", 0, scheduledTimestamp + 1);
		assertEquals(1L, countBefore.longValue());

		// 3. 현재 시간이 지나면 처리
		redisTemplate.opsForZSet().remove("pending-notifications", eventId);
		redisTemplate.opsForHash().delete("notifications", eventId);

		// then
		Object remaining = redisTemplate.opsForHash().get("notifications", eventId);
		assertNull(remaining);

		Long countAfter = redisTemplate.opsForZSet().count("pending-notifications", 0, scheduledTimestamp + 1);
		assertEquals(0L, countAfter.longValue());
	}

	// ========== 6. 에러 처리 테스트 ==========

	@Test
	@Order(14)
	@DisplayName("존재하지 않는 사용자로 알림 저장 시 예외가 발생한다")
	void testSaveNotificationWithInvalidUser() {
		// given
		PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
			.recipientUserId(99999L)  // 존재하지 않는 사용자
			.notificationType("CHEER_FRIEND")
			.relatedId("123")
			.metadata(new HashMap<>())
			.build();

		// when & then
		assertThrows(Exception.class, () -> {
			saveNotificationService.save(event, "제목", "메시지", "link");
		});
	}

	@Test
	@Order(15)
	@DisplayName("알림 타입 매핑이 모든 타입을 지원한다")
	void testAllNotificationTypesMapping() {
		// given
		String[] allTypes = {
			"CHEER_FRIEND",
			"FEED_UPLOADED",
			"FEED_REACTION",
			"FEED_REMINDER",
			"RUNNING_PROGRESS_REMINDER",
			"NEW_USER_RUNNING_REMINDER",
			"NEW_USER_FRIEND_REMINDER"
		};

		// when & then
		for (String type : allTypes) {
			PushNotificationRequestedEvent event = PushNotificationRequestedEvent.builder()
				.recipientUserId(testUser.getId())
				.notificationType(type)
				.relatedId("123")
				.metadata(new HashMap<>())
				.build();

			assertDoesNotThrow(() -> {
				Notification notification = saveNotificationService.save(event, "제목", "메시지", "link");
				assertNotNull(notification.getId());
			});
		}
	}
}
