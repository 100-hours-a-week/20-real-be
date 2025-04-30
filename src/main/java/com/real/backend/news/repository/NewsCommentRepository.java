package com.real.backend.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.news.domain.NewsComment;

public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {
}
