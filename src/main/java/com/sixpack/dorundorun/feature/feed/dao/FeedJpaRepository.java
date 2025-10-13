package com.sixpack.dorundorun.feature.feed.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.feed.domain.Feed;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {
}
