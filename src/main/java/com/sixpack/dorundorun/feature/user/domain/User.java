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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "running_level", nullable = false)
	private String runningLevel;

	@Column(name = "marketing_consent_at")
	private LocalDateTime marketingConsentAt;

	@Column(name = "location_consent_at")
	private LocalDateTime locationConsentAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public User(String name, String email, String password, String nickname, String runningLevel,
		LocalDateTime marketingConsentAt, LocalDateTime locationConsentAt) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.runningLevel = runningLevel;
		this.marketingConsentAt = marketingConsentAt;
		this.locationConsentAt = locationConsentAt;
	}
}
