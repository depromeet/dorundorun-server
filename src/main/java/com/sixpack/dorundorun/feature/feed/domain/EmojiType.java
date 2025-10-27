package com.sixpack.dorundorun.feature.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmojiType {
	SURPRISE("놀람"),
	HEART("하트"),
	THUMBS_UP("따봉"),
	CONGRATS("축하"),
	FIRE("불");

	private final String description;
}
