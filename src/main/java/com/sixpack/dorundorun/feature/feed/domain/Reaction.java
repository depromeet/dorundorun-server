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

	/**
	 * 이 반응이 활성화 상태인지 확인합니다.
	 *
	 * @return deletedAt이 null이면 true (활성화), null이 아니면 false (비활성화)
	 */
	public boolean isActive() {
		return deletedAt == null;
	}

	/**
	 * 반응을 활성화(재활성화)합니다.
	 * 소프트 삭제된 반응을 다시 활성화할 때 사용합니다.
	 *
	 * <p>동작:
	 * <ul>
	 *   <li>deletedAt을 null로 설정</li>
	 *   <li>기존 레코드를 재사용하여 이력 보존</li>
	 * </ul>
	 */
	public void activate() {
		this.deletedAt = null;
	}

	/**
	 * 반응을 비활성화(소프트 삭제)합니다.
	 * 물리적 삭제 대신 deletedAt 타임스탬프를 기록합니다.
	 *
	 * <p>동작:
	 * <ul>
	 *   <li>deletedAt에 현재 시간 설정</li>
	 *   <li>레코드는 DB에 유지되어 이력 추적 가능</li>
	 *   <li>활성화된 반응 조회 시 제외됨</li>
	 * </ul>
	 */
	public void deactivate() {
		this.deletedAt = LocalDateTime.now();
	}
}
