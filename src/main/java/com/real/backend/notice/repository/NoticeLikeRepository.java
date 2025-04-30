package com.real.backend.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.notice.domain.NoticeLike;

public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {
}
