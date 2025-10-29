package com.sixpack.dorundorun.feature.notification.event.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.application.PushNotificationContentDeterminer;
import com.sixpack.dorundorun.feature.notification.application.SaveNotificationService;
import com.sixpack.dorundorun.feature.notification.application.SendPushNotificationService;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class PushNotificationEventListener extends AbstractRedisStreamEventHandler<PushNotificationRequestedEvent> {

	private final SaveNotificationService saveNotificationService;
	private final SendPushNotificationService sendPushNotificationService;
	private final PushNotificationContentDeterminer contentDeterminer;

	public PushNotificationEventListener(
		ObjectMapper objectMapper,
		SaveNotificationService saveNotificationService,
		SendPushNotificationService sendPushNotificationService,
		PushNotificationContentDeterminer contentDeterminer
	) {
		super(objectMapper);
		this.saveNotificationService = saveNotificationService;
		this.sendPushNotificationService = sendPushNotificationService;
		this.contentDeterminer = contentDeterminer;
	}

	@Override
	public String getEventType() {
		return PushNotificationRequestedEvent.TYPE;
	}

	@Override
	protected Class<PushNotificationRequestedEvent> payloadType() {
		return PushNotificationRequestedEvent.class;
	}

	@Override
	protected void onMessage(PushNotificationRequestedEvent event) throws Exception {
		log.info("Processing push notification event: recipientId={}, type={}",
			event.recipientUserId(), event.notificationType());

		try {
			// 알림 콘텐츠 결정
			String title = contentDeterminer.determineTitle(event.notificationType(), event.metadata());
			String message = contentDeterminer.determineMessage(event.notificationType(), event.metadata());
			String deepLink = contentDeterminer.determineDeepLink(event.notificationType(), event.relatedId(),
				event.metadata());

			// 알림을 DB에 저장
			saveNotificationService.save(event, title, message, deepLink);

			// 푸시 알림 발송
			sendPushNotificationService.send(event, title, message, deepLink);

			log.info("Push notification processed successfully: recipientId={}, type={}, deepLink={}",
				event.recipientUserId(), event.notificationType(), deepLink);

		} catch (Exception e) {
			log.error("Failed to process push notification: recipientId={}, type={}",
				event.recipientUserId(), event.notificationType(), e);
			throw e;
		}
	}

}
