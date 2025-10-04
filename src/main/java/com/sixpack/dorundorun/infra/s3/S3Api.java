package com.sixpack.dorundorun.infra.s3;

import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[DEV:S3] 파일 관리")
public interface S3Api {

	@Operation(summary = "파일 업로드 테스트 API")
	@ApiResponse(responseCode = "200", description = "파일 업로드 성공",
		content = @Content(mediaType = "application/json",
			schema = @Schema(type = "string")))
	DorunResponse<Void> uploadFile(@Parameter(description = "업로드할 파일") MultipartFile file);

	@Operation(summary = "파일 URL 조회 API")
	@ApiResponse(responseCode = "200", description = "파일 URL 조회 성공")
	@ApiResponse(responseCode = "500", description = "S3 조회 실패")
	DorunResponse<Void> getFileUrl();
}