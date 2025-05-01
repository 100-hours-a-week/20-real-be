package com.real.backend.domain.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.domain.news.domain.NewsLike;

public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {
}
