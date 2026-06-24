package com.sixpack.dorundorun.infra.firebase;

import java.util.List;

// unregisteredTokens: UNREGISTERED(영구 무효)로 실패한 토큰 목록.
// 호출부가 해당 토큰의 소유자를 찾아 무효화할 수 있도록 별도로 노출한다.
public record FcmMulticastResult(
	List<String> successMessageIds,
	List<String> unregisteredTokens
) {
}
