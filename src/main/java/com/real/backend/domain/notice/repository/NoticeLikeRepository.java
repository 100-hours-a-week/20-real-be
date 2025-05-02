package com.real.backend.domain.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.domain.notice.domain.NoticeLike;

public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {
}
