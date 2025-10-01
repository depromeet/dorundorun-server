package com.sixpack.dorundorun.feature.user.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "running_level")
	private RunningLevel runningLevel;

	@Column(name = "marketing_consent_at")
	private LocalDateTime marketingConsentAt;

	@Column(name = "location_consent_at")
	private LocalDateTime locationConsentAt;

	@Column(name = "personal_consent_at")
	private LocalDateTime personalConsentAt;

	@Column(name = "height")
	private Integer height;

	@Column(name = "weight")
	private Integer weight;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public User(String name,
		String email,
		String password,
		String nickname,
		RunningLevel runningLevel,
		LocalDateTime marketingConsentAt,
		LocalDateTime locationConsentAt,
		LocalDateTime personalConsentAt,
		Integer height,
		Integer weight,
		LocalDateTime deletedAt) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.runningLevel = runningLevel;
		this.marketingConsentAt = marketingConsentAt;
		this.locationConsentAt = locationConsentAt;
		this.personalConsentAt = personalConsentAt;
		this.height = height;
		this.weight = weight;
		this.deletedAt = deletedAt;
	}
}
