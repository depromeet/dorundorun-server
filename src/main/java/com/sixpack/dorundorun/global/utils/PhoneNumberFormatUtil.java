package com.sixpack.dorundorun.global.utils;

public class PhoneNumberFormatUtil {

	private PhoneNumberFormatUtil() {
	}

	public static String format(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			return phoneNumber;
		}

		String digitsOnly = phoneNumber.replaceAll("-", "");

		if (digitsOnly.length() != 11) {
			return phoneNumber;
		}

		return digitsOnly.substring(0, 3) + "-" +
		       digitsOnly.substring(3, 7) + "-" +
		       digitsOnly.substring(7, 11);
	}
}
