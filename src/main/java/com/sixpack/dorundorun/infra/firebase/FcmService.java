package com.sixpack.dorundorun.infra.firebase;

public interface FcmService {

	String sendMessage(FcmMessage message);

	java.util.List<String> sendMulticastMessage(FcmMessage message, java.util.List<String> deviceTokens);

	boolean isEnabled();

	boolean isValidToken(String deviceToken);
}
