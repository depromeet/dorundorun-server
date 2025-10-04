package com.sixpack.dorundorun.infra.s3;

import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.exception.ErrorCode;

public class S3OperationException extends CustomException {

	public S3OperationException(ErrorCode errorCode) {
		super(errorCode);
	}

	public S3OperationException(ErrorCode errorCode, String detailMessage) {
		super(errorCode, detailMessage);
	}
}
