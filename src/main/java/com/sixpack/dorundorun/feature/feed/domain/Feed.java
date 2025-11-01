package com.sixpack.dorundorun.feature.feed.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.utils.S3ImageUrlUtil;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

	@BatchSize(size = 50)
	@OneToMany(mappedBy = "feed", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Reaction> reactions = new ArrayList<>();

	public String getMapImageUrl() {
		return S3ImageUrlUtil.getPresignedImageUrl(this.mapImage);
	}

	public String getSelfieImageUrl() {
		// 셀피 이미지가 없으면 맵 이미지 URL 반환
		if (this.selfieImage == null) {
			return getMapImageUrl();
		}
		return S3ImageUrlUtil.getPresignedImageUrl(this.selfieImage);
	}

	public String getSelfieImageKey() {
		return this.selfieImage;
	}

	public void update(String content, String selfieImage) {
		this.content = content;
		this.selfieImage = selfieImage;  // null이면 이미지 삭제
	}
}
