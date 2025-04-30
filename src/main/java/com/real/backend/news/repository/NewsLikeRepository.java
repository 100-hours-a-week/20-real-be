package com.real.backend.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.news.domain.NewsLike;

public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {
}
