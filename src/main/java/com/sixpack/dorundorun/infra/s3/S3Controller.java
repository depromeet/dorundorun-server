package com.sixpack.dorundorun.infra.s3;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
@RestController
@RequiredArgsConstructor
public class S3Controller implements S3Api {

	private final S3Service s3Service;

	@PostMapping(value = "/api/s3/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<Void> uploadFile(@RequestPart("file") MultipartFile file) {
		try {
			String uploadedKey = s3Service.uploadImage(file, "test");
			return DorunResponse.success(uploadedKey);
		} catch (Exception e) {
			log.error("File upload failed", e);
			throw e;
		}
	}

	@GetMapping("/api/s3/url")
	public DorunResponse<Void> getFileUrl() {
		String key = "test/7b2b89c7-8ea1-42bf-b318-ce7ec6029c0c.png";
		String fileUrl = s3Service.getImageUrl(key);
		return DorunResponse.success(fileUrl);
	}
}
