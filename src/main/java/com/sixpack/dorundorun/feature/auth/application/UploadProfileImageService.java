package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadProfileImageService {

	private final S3Service s3Service;

	public String upload(MultipartFile profileImage) {
		if (profileImage == null || profileImage.isEmpty()) {
			return null;
		}

		return s3Service.uploadImage(profileImage);
	}
}
