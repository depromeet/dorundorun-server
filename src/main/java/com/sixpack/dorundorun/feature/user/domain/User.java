package com.sixpack.dorundorun.feature.user.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "phone_number", nullable = false, unique = true)
	private String phoneNumber;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "code", nullable = false)
	private String code;

	@Column(name = "marketing_consent_at")
	private LocalDateTime marketingConsentAt;

	@Column(name = "location_consent_at")
	private LocalDateTime locationConsentAt;

	@Column(name = "personal_consent_at")
	private LocalDateTime personalConsentAt;

	@Column(name = "device_token", nullable = false)
	private String deviceToken;

	@Column(name = "alarm_consent_at")
	private LocalDateTime alarmConsentAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
}
