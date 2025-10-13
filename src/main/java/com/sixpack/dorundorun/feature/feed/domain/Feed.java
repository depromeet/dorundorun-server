package com.sixpack.dorundorun.feature.feed.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Feed extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "run_session_id", nullable = false)
	private RunSession runSession;

	@Column(name = "map_image", nullable = false)
	private String mapImage;

	@Column(name = "selfie_image")
	private String selfieImage;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	public Feed(User user,
		RunSession runSession,
		String mapImage,
		String selfieImage,
		String content,
		LocalDateTime deletedAt) {
		this.user = user;
		this.runSession = runSession;
		this.mapImage = mapImage;
		this.selfieImage = selfieImage;
		this.content = content;
		this.deletedAt = deletedAt;
	}
}
