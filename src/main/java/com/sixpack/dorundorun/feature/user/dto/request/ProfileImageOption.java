package com.sixpack.dorundorun.feature.user.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProfileImageOption {
	SET,
	REMOVE,
	KEEP;

	@JsonCreator
	public static ProfileImageOption from(String value) {
		if (value == null) {
			return KEEP;
		}
		return ProfileImageOption.valueOf(value.toUpperCase());
	}
}
