package com.sixpack.dorundorun.global.utils;

import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.infra.s3.S3Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3ImageUrlUtil {

	private final S3Service s3Service;
	private static S3Service staticS3Service;

	@PostConstruct
	public void init() {
		staticS3Service = s3Service;
	}

	public static String getPresignedImageUrl(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		return staticS3Service.getImageUrl(key);
	}
}
