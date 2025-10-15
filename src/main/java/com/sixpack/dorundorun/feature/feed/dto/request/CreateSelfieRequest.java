package com.sixpack.dorundorun.feature.feed.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "셀피 등록 요청")
public class CreateSelfieRequest {

	@Schema(description = "인증 날짜", example = "2025-09-20")
	@NotBlank(message = "인증 날짜는 필수입니다.")
	private String date;

	@Schema(description = "인증 시간", example = "2025-09-20T23:58:00")
	@NotNull(message = "인증 시간은 필수입니다.")
	private LocalDateTime selfieTime;

	@Schema(description = "총 달린 거리 (km)", example = "5.10")
	@NotNull(message = "총 달린 거리는 필수입니다.")
	@Positive(message = "총 달린 거리는 양수여야 합니다.")
	private Double totalDistance;

	@Schema(description = "총 달린 시간 (초)", example = "2647")
	@NotNull(message = "총 달린 시간은 필수입니다.")
	@Positive(message = "총 달린 시간은 양수여야 합니다.")
	private Integer totalTime;

	@Schema(description = "평균 페이스", example = "7'30\"")
	@NotBlank(message = "평균 페이스는 필수입니다.")
	private String averagePace;

	@Schema(description = "케이던스 (spm)", example = "144")
	@NotNull(message = "케이던스는 필수입니다.")
	@Positive(message = "케이던스는 양수여야 합니다.")
	private Integer cadence;

	@Schema(description = "셀피 이미지 URL", example = "https://example.com/images/selfie123.jpg")
	@NotBlank(message = "이미지 URL은 필수입니다.")
	private String imageUrl;

	public CreateSelfieRequest(String date, LocalDateTime selfieTime, Double totalDistance, 
							Integer totalTime, String averagePace, Integer cadence, String imageUrl) {
		this.date = date;
		this.selfieTime = selfieTime;
		this.totalDistance = totalDistance;
		this.totalTime = totalTime;
		this.averagePace = averagePace;
		this.cadence = cadence;
		this.imageUrl = imageUrl;
	}
}
