package com.sixpack.dorundorun.feature.feed.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "인증피드 등록 요청")
public class CreateSelfieRequest {

	@Schema(description = "인증 이미지 파일")
	@NotNull(message = "이미지 파일은 필수입니다.")
	private MultipartFile image;

	@Schema(description = "인증 시간", example = "2025-09-20T23:58:00")
	@NotNull(message = "인증 시간은 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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

	public CreateSelfieRequest(MultipartFile image, LocalDateTime selfieTime,
		Double totalDistance, Integer totalTime, String averagePace, Integer cadence) {
		this.image = image;
		this.selfieTime = selfieTime;
		this.totalDistance = totalDistance;
		this.totalTime = totalTime;
		this.averagePace = averagePace;
		this.cadence = cadence;
	}
}
