package com.sixpack.dorundorun.feature.feed.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmojiType {
	THUMBS_UP("👍", "좋아요"),
	CLAP("👏", "박수"),
	FIRE("🔥", "불타는"),
	HEART("❤️", "하트"),
	MUSCLE("💪", "힘내");

	private final String emoji;
	private final String description;
}
