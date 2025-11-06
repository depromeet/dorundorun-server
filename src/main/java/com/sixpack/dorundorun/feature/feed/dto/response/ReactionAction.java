package com.sixpack.dorundorun.feature.feed.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionAction {
	ADDED("추가"),
	REMOVED("제거");

	private final String description;
}
