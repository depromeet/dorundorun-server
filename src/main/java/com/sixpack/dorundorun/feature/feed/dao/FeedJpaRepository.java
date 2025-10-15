package com.sixpack.dorundorun.feature.feed.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.feed.domain.Feed;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {
	
	Optional<Feed> findByRunSessionIdAndDeletedAtIsNull(Long runSessionId);
}
