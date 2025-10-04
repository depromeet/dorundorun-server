package com.sixpack.dorundorun.global.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
	String accessKey,
	String secretKey,
	String region,
	String bucketName,
	int signatureDurationDay,
	long maxFileSizeMb
) {
}
