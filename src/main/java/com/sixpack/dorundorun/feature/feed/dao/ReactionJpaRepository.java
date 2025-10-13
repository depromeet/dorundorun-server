package com.sixpack.dorundorun.feature.feed.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.feed.domain.Reaction;

public interface ReactionJpaRepository extends JpaRepository<Reaction, Long> {
}
