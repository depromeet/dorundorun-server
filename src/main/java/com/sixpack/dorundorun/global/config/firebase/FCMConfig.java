package com.sixpack.dorundorun.global.config.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FirebaseProperties.class)
@ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "true")
public class FCMConfig {

	private final FirebaseProperties firebaseProperties;

	private static final String FIREBASE_JSON_ENV_KEY = "FIREBASE_SERVICE_ACCOUNT_JSON";

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}

		String serviceAccountPath = firebaseProperties.config().serviceAccountPath();
		String projectId = firebaseProperties.config().projectId();

		GoogleCredentials credentials = loadGoogleCredentials(serviceAccountPath);

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(credentials)
			.setProjectId(projectId)
			.build();

		return FirebaseApp.initializeApp(options);
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance();
	}

	private GoogleCredentials loadGoogleCredentials(String serviceAccountPath) throws IOException {
		// ENV에서 우선 로드
		String envJson = System.getenv(FIREBASE_JSON_ENV_KEY);
		if (envJson != null && !envJson.isBlank()) {
			InputStream stream = new ByteArrayInputStream(envJson.getBytes(StandardCharsets.UTF_8));
			return GoogleCredentials.fromStream(stream);
		}

		// 파일에서 로드
		if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
			throw new IOException("Firebase service account path is not configured");
		}

		Resource resource = serviceAccountPath.startsWith("classpath:")
			? new ClassPathResource(serviceAccountPath.substring("classpath:".length()))
			: new FileSystemResource(serviceAccountPath);

		if (!resource.exists()) {
			throw new IOException("Firebase service account file not found at: " + serviceAccountPath);
		}

		return GoogleCredentials.fromStream(resource.getInputStream());
	}
}
