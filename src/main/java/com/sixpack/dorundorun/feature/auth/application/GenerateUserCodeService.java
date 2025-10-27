package com.sixpack.dorundorun.feature.auth.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class GenerateUserCodeService {

	public String generate() {
		return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}
