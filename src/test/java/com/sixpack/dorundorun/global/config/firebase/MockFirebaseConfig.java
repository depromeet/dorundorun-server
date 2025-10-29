package com.sixpack.dorundorun.global.config.firebase;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.google.firebase.messaging.FirebaseMessaging;

@TestConfiguration
public class MockFirebaseConfig {

	@Bean("testFirebaseMessaging")
	@Primary
	public FirebaseMessaging firebaseMessaging() {
		return Mockito.mock(FirebaseMessaging.class);
	}
}
