package com.real.backend.domain.news.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.domain.NewsLike;
import com.real.backend.domain.user.domain.User;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {
    Optional<NewsLike> findByNewsAndUser(News news, User user);
}

