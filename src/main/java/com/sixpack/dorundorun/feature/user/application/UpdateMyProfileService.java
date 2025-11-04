package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.request.MyProfileUpdateRequest;
import com.sixpack.dorundorun.feature.user.dto.request.ProfileImageOption;
import com.sixpack.dorundorun.feature.user.dto.response.NewProfileResponse;
import com.sixpack.dorundorun.feature.user.exception.UserErrorCode;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateMyProfileService {

	private final S3Service s3Service;
	private final ObjectMapper objectMapper;
	private final UpdateUserProfileTransactionService transactionService;

	public NewProfileResponse updateProfile(User user, String dataJson, MultipartFile profileImage) {
		MyProfileUpdateRequest request = parseRequest(dataJson);
		ProfileImageOption option = request.imageOption() != null ? request.imageOption() : ProfileImageOption.KEEP;

		String currentImageUrl = user.getProfileImageUrl();
		String newImageUrl = determineNewImageUrl(option, profileImage, currentImageUrl);

		transactionService.updateProfile(user, request.nickname(), newImageUrl);

		deleteOldImageIfNeeded(option, currentImageUrl);

		if (newImageUrl == null) {
			return null;
		}
		return new NewProfileResponse(s3Service.getImageUrl(newImageUrl));
	}

	private MyProfileUpdateRequest parseRequest(String dataJson) {
		try {
			return objectMapper.readValue(dataJson, MyProfileUpdateRequest.class);
		} catch (JsonProcessingException e) {
			throw new CustomException(UserErrorCode.INVALID_PROFILE_UPDATE_REQUEST);
		}
	}

	private String determineNewImageUrl(ProfileImageOption option, MultipartFile profileImage,
		String currentImageUrl) {
		return switch (option) {
			case SET -> {
				validateImageFile(profileImage);
				yield s3Service.uploadImage(profileImage);
			}
			case REMOVE -> null;
			case KEEP -> currentImageUrl;
		};
	}

	private void validateImageFile(MultipartFile profileImage) {
		if (profileImage == null || profileImage.isEmpty()) {
			throw new CustomException(UserErrorCode.PROFILE_IMAGE_REQUIRED);
		}
	}

	private void deleteOldImageIfNeeded(ProfileImageOption option, String oldImageUrl) {
		if (option == ProfileImageOption.KEEP) {
			return;
		}

		if (oldImageUrl == null || oldImageUrl.isEmpty()) {
			return;
		}

		try {
			s3Service.deleteImage(oldImageUrl);
			log.info("Old profile image deleted: {}", oldImageUrl);
		} catch (Exception e) {
			log.warn("Failed to delete old profile image: {}", e.getMessage());
		}
	}
}
