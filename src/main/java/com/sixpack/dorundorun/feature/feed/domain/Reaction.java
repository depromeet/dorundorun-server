package com.sixpack.dorundorun.feature.feed.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Reaction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id", nullable = false)
	private Feed feed;

	@Enumerated(EnumType.STRING)
	@Column(name = "emoji_type", nullable = false)
	private EmojiType emojiType;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public boolean isActive() {
		return deletedAt == null;
	}

	public void activate() {
		this.deletedAt = null;
	}

	public void deactivate() {
		this.deletedAt = LocalDateTime.now();
	}
}
