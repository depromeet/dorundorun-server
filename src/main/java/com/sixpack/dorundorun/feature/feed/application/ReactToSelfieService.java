package com.sixpack.dorundorun.feature.feed.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dao.ReactionJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.ReactionAction;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

/**
 * 셀피(인증 피드)에 대한 반응(이모지) 추가/제거를 처리하는 서비스
 *
 * <p>주요 기능:
 * <ul>
 *   <li>사용자가 특정 피드에 이모지 반응을 남김</li>
 *   <li>이미 반응을 남긴 경우 토글(추가 → 제거, 제거 → 추가) 방식으로 동작</li>
 *   <li>소프트 삭제(deletedAt) 패턴으로 데이터 정합성 유지</li>
 * </ul>
 *
 * <p>데이터 정합성 전략:
 * <ul>
 *   <li>동일한 (user, feed, emojiType) 조합은 DB에 하나만 존재</li>
 *   <li>삭제 시 물리 삭제가 아닌 deletedAt 타임스탬프로 소프트 삭제</li>
 *   <li>재활성화 시 deletedAt을 null로 설정하여 기존 레코드 재사용</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReactToSelfieService {

	private final FeedJpaRepository feedJpaRepository;
	private final ReactionJpaRepository reactionJpaRepository;

	/**
	 * 셀피에 대한 반응을 토글(추가/제거)합니다.
	 *
	 * <p>실행 흐름:
	 * <ol>
	 *   <li>피드 존재 여부 및 삭제 여부 검증</li>
	 *   <li>사용자의 기존 반응 이력 조회 (삭제된 것 포함)</li>
	 *   <li>토글 로직 실행:
	 *     <ul>
	 *       <li>반응 이력 없음 → 새로 생성 (ADDED)</li>
	 *       <li>반응 활성화 상태 → 비활성화 (REMOVED)</li>
	 *       <li>반응 비활성화 상태 → 재활성화 (ADDED)</li>
	 *     </ul>
	 *   </li>
	 *   <li>현재 피드의 전체 활성화된 반응 수 집계</li>
	 *   <li>응답 DTO 생성 및 반환</li>
	 * </ol>
	 *
	 * @param user 현재 로그인한 사용자
	 * @param request 피드 ID와 이모지 타입을 포함한 요청 데이터
	 * @return 수행된 액션(ADDED/REMOVED), 이모지 타입, 전체 반응 수를 포함한 응답
	 * @throws com.sixpack.dorundorun.global.exception.CustomException 피드를 찾을 수 없거나 삭제된 경우
	 */
	@Transactional
	public SelfieReactionResponse execute(User user, SelfieReactionRequest request) {
		// 1. 피드 검증: 존재하고 삭제되지 않은 피드인지 확인
		Feed feed = feedJpaRepository.findById(request.feedId())
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(request.feedId()));

		// 2. 기존 반응 조회: 해당 사용자가 이 피드에 같은 이모지로 반응한 이력이 있는지 확인
		//    - deletedAt이 null이 아닌 것도 포함 (소프트 삭제된 반응도 재활성화 가능)
		//    - 동일한 (user, feed, emojiType) 조합은 최대 1개만 존재 (유니크 제약)
		Optional<Reaction> existingReaction = reactionJpaRepository.findByFeedIdAndUserIdAndEmojiType(
			request.feedId(),
			user.getId(),
			request.emojiType()
		);

		// 3. 토글 로직 실행: 기존 반응 상태에 따라 추가/제거 처리
		ReactionAction action = toggleReaction(existingReaction, user, feed, request);

		// 4. 현재 피드의 활성화된 반응 총 개수 조회
		//    - deletedAt이 null인 반응만 카운트
		int totalReactionCount = reactionJpaRepository.countByFeedIdAndDeletedAtIsNull(request.feedId());

		// 5. 응답 DTO 생성
		return new SelfieReactionResponse(
			request.feedId(),
			request.emojiType().name(),
			action,
			totalReactionCount
		);
	}

	/**
	 * 반응 토글 로직을 처리합니다 (추가 ↔ 제거).
	 *
	 * <p>동작 시나리오:
	 * <pre>
	 * 시나리오 1: 반응 이력 없음
	 *   → Reaction 엔티티 새로 생성하여 저장
	 *   → 결과: ADDED
	 *
	 * 시나리오 2: 반응이 활성화 상태 (deletedAt == null)
	 *   → deactivate() 호출로 deletedAt에 현재 시간 설정
	 *   → 결과: REMOVED
	 *
	 * 시나리오 3: 반응이 비활성화 상태 (deletedAt != null)
	 *   → activate() 호출로 deletedAt을 null로 설정
	 *   → 기존 레코드 재사용 (이력 보존)
	 *   → 결과: ADDED
	 * </pre>
	 *
	 * <p>데이터 정합성 보장:
	 * <ul>
	 *   <li>물리 삭제 대신 소프트 삭제로 이력 관리</li>
	 *   <li>동일한 반응을 여러 번 추가/제거해도 데이터 무결성 유지</li>
	 *   <li>트랜잭션 내에서 처리되어 동시성 문제 방지</li>
	 * </ul>
	 *
	 * @param existingReaction 기존 반응 이력 (Optional)
	 * @param user 현재 사용자
	 * @param feed 대상 피드
	 * @param request 요청 데이터
	 * @return 수행된 액션 (ADDED 또는 REMOVED)
	 */
	private ReactionAction toggleReaction(Optional<Reaction> existingReaction, User user, Feed feed,
		SelfieReactionRequest request) {
		if (existingReaction.isPresent()) {
			// 기존 반응이 존재하는 경우
			Reaction reaction = existingReaction.get();

			if (reaction.isActive()) {
				// 현재 활성화 상태 → 비활성화 (소프트 삭제)
				// deletedAt에 현재 시간 설정
				reaction.deactivate();
				return ReactionAction.REMOVED;
			} else {
				// 현재 비활성화 상태 → 재활성화
				// deletedAt을 null로 설정하여 기존 레코드 재사용
				// 이력을 보존하면서도 다시 활성화 가능
				reaction.activate();
				return ReactionAction.ADDED;
			}
		} else {
			// 기존 반응이 없는 경우 → 새로운 Reaction 엔티티 생성
			// deletedAt은 null로 초기화되어 활성화 상태로 생성됨
			Reaction newReaction = Reaction.builder()
				.user(user)
				.feed(feed)
				.emojiType(request.emojiType())
				.build();
			reactionJpaRepository.save(newReaction);
			return ReactionAction.ADDED;
		}
	}
}