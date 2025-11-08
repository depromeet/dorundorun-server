package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.request.DeviceTokenUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateDeviceTokenService {

	@Transactional
	public void updateDeviceToken(User user, DeviceTokenUpdateRequest request) {
		user.updateDeviceToken(request.deviceToken());
	}
}
