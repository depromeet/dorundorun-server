package com.sixpack.dorundorun.feature.friend.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FriendErrorCode implements ErrorCode {

	NOT_FOUND_USER_BY_CODE(HttpStatus.NOT_FOUND, "해당 코드의 유저를 찾을 수 없습니다. [code: %s]"),
	NOT_FOUND_FRIEND(HttpStatus.NOT_FOUND, "해당 친구를 찾을 수 없습니다. [friendId: %s]"),
	
	ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "이미 친구로 등록된 유저입니다. [userId: %s]"),
	CANNOT_ADD_SELF_AS_FRIEND(HttpStatus.BAD_REQUEST, "자기 자신을 친구로 추가할 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
