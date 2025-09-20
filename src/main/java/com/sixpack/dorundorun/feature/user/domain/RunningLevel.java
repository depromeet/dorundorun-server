package com.sixpack.dorundorun.feature.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RunningLevel {
	BEGINNER("이제 막 시작했어요", "최근 달린 경험이 없어요."),
	OCCASIONAL("가끔 달려요", "주 1-2회 이하로 가볍게 달려요."),
	CONSISTENT("꾸준히 달려요", "주 3회 이상 루틴대로 달려요.");

	private final String title;
	private final String description;
}
