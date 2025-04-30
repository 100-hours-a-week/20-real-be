package com.real.backend.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.news.domain.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}
