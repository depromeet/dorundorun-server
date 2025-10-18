package com.sixpack.dorundorun.feature.user.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GetDefaultProfileImageUrlService {

	@Value("${app.default-profile-image-url}")
	private String defaultProfileImageUrl;

	public String get() {
		return defaultProfileImageUrl;
	}
}
