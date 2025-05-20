package com.real.backend.domain.news.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.domain.NewsLike;
import com.real.backend.domain.user.domain.User;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {
    Optional<NewsLike> findByNewsAndUser(News news, User user);

    @Modifying
    @Query(value = """
    INSERT INTO news_like (user_id, news_id, is_activated)
    VALUES (:userId, :newsId, true)
    ON DUPLICATE KEY UPDATE
    is_activated = NOT is_activated
""", nativeQuery = true)
    void insertOrToggle(@Param("userId") Long userId, @Param("newsId") Long newsId);
}

