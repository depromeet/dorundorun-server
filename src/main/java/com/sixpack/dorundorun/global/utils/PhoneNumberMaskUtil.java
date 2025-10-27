package com.sixpack.dorundorun.global.utils;

public class PhoneNumberMaskUtil {

	private PhoneNumberMaskUtil() {
	}

	public static String mask(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			return phoneNumber;
		}

		String digitsOnly = phoneNumber.replaceAll("-", "");

		if (digitsOnly.length() != 11) {
			return phoneNumber;
		}

		String prefix = digitsOnly.substring(0, 3);
		String suffix = digitsOnly.substring(7, 11);

		if (phoneNumber.contains("-")) {
			return prefix + "-****-" + suffix;
		} else {
			return prefix + "****" + suffix;
		}
	}
}