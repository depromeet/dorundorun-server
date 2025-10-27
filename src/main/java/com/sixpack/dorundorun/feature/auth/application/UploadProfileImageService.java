package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadProfileImageService {

	private final S3Service s3Service;

	@Value("${app.default-profile-image-url}")
	private String defaultProfileImageUrl;

	public String upload(MultipartFile profileImage) {
		if (profileImage == null || profileImage.isEmpty()) {
			return defaultProfileImageUrl;
		}

		return s3Service.uploadImage(profileImage);
	}
}