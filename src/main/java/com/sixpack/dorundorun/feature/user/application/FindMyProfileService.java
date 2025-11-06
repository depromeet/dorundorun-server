package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.MyProfileResponse;
import com.sixpack.dorundorun.global.utils.PhoneNumberFormatUtil;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindMyProfileService {

	private final S3Service s3Service;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	public MyProfileResponse find(User user) {
		String presignedImageUrl = user.getProfileImageUrl() != null
			? s3Service.getImageUrl(user.getProfileImageUrl())
			: getDefaultProfileImageUrlService.get();

		String formattedPhoneNumber = PhoneNumberFormatUtil.format(user.getPhoneNumber());

		return new MyProfileResponse(
			user.getId(),
			user.getNickname(),
			presignedImageUrl,
			user.getCode(),
			formattedPhoneNumber,
			user.getCreatedAt()
		);
	}
}
