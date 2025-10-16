package com.sixpack.dorundorun.infra.s3;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

	String uploadImage(MultipartFile file);

	String uploadImage(MultipartFile file, String folder);

	String getImageUrl(String key);

	InputStream downloadImage(String key);

	void deleteImage(String key);
}
