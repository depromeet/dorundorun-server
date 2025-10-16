package com.sixpack.dorundorun.infra.s3;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.global.config.s3.S3Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";
	private static final String FOLDER_SEPARATOR = "/";
	public static final String DORUNDORUN_FOLDER = "dorundorun";

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final S3Properties s3Properties;

	@Override
	public String uploadImage(MultipartFile file) {
		return this.uploadImage(file, DORUNDORUN_FOLDER);
	}

	@Override
	public String uploadImage(MultipartFile file, String folder) {
		validateFile(file);

		String key = buildS3Key(folder, generateFileName(file.getOriginalFilename()));

		try {
			PutObjectRequest request = createPutObjectRequest(key, file);
			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
			return key;
		} catch (IOException e) {
			log.error("Failed to upload file to S3: {}", e.getMessage());
			throw new S3OperationException(S3ErrorCode.S3_UPLOAD_FAILED);
		} catch (S3Exception e) {
			log.error("S3 error occurred: {}", e.getMessage());
			throw new S3OperationException(S3ErrorCode.S3_UPLOAD_FAILED);
		}
	}

	@Override
	public String getImageUrl(String key) {
		try {
			GetObjectRequest request = createGetObjectRequest(key);
			GetObjectPresignRequest presignRequest = createPresignRequest(request);

			return s3Presigner.presignGetObject(presignRequest).url().toString();

		} catch (S3Exception e) {
			log.error("Failed to generate presigned URL for key {}: {}", key, e.getMessage());
			throw new S3OperationException(S3ErrorCode.S3_URL_GENERATION_FAILED);
		}
	}

	@Override
	public InputStream downloadImage(String key) {
		try {
			GetObjectRequest request = createGetObjectRequest(key);
			return s3Client.getObject(request);

		} catch (S3Exception e) {
			log.error("Failed to download file from S3: {}", e.getMessage());
			throw new S3OperationException(S3ErrorCode.S3_DOWNLOAD_FAILED);
		}
	}

	@Override
	public void deleteImage(String key) {
		try {
			DeleteObjectRequest request = createDeleteObjectRequest(key);
			s3Client.deleteObject(request);
			log.info("Successfully deleted file from S3: {}", key);

		} catch (S3Exception e) {
			log.error("Failed to delete file from S3: {}", e.getMessage());
			throw new S3OperationException(S3ErrorCode.S3_DELETE_FAILED);
		}
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new S3OperationException(S3ErrorCode.FILE_IS_EMPTY);
		}

		validateContentType(file.getContentType());
		validateFileSize(file.getSize());
	}

	private void validateContentType(String contentType) {
		if (contentType == null || !contentType.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
			throw new S3OperationException(S3ErrorCode.INVALID_FILE_FORMAT);
		}
	}

	private void validateFileSize(long fileSize) {
		long maxFileSizeBytes = s3Properties.maxFileSizeMb() * 1024 * 1024;
		if (fileSize > maxFileSizeBytes) {
			throw new S3OperationException(S3ErrorCode.FILE_SIZE_EXCEEDED,
				"파일 크기가 최대 제한 " + s3Properties.maxFileSizeMb() + "MB를 초과했습니다.");
		}
	}

	private String generateFileName(String originalFilename) {
		String extension = getFileExtension(originalFilename);
		return UUID.randomUUID() + extension;
	}

	private String getFileExtension(String filename) {
		if (filename == null || filename.isEmpty()) {
			return "";
		}
		int lastDotIndex = filename.lastIndexOf(".");
		return lastDotIndex != -1 ? filename.substring(lastDotIndex) : "";
	}

	private String buildS3Key(String folder, String fileName) {
		return folder + FOLDER_SEPARATOR + fileName;
	}

	private PutObjectRequest createPutObjectRequest(String key, MultipartFile file) {
		return PutObjectRequest.builder()
			.bucket(s3Properties.bucketName())
			.key(key)
			.contentType(file.getContentType())
			.contentLength(file.getSize())
			.build();
	}

	private GetObjectRequest createGetObjectRequest(String key) {
		return GetObjectRequest.builder()
			.bucket(s3Properties.bucketName())
			.key(key)
			.build();
	}

	private GetObjectPresignRequest createPresignRequest(GetObjectRequest request) {
		return GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofDays(s3Properties.signatureDurationDay()))
			.getObjectRequest(request)
			.build();
	}

	private DeleteObjectRequest createDeleteObjectRequest(String key) {
		return DeleteObjectRequest.builder()
			.bucket(s3Properties.bucketName())
			.key(key)
			.build();
	}
}
