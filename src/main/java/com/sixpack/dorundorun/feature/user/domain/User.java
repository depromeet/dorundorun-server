package com.sixpack.dorundorun.feature.user.domain;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public User(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}
}
