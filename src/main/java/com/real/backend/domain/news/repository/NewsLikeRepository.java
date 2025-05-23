package com.real.backend.domain.news.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.news.domain.NewsLike;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {
    Optional<NewsLike> findByUserIdAndNewsId(Long userId, Long newsId);

    @Modifying
    @Query(value = """
    UPDATE NewsLike n
    SET n.isActivated = :isActivated
    WHERE n.news.id = :newsId AND n.user.id = :userId
""")
    void updateIsActivated(@Param("userId") Long userId, @Param("newsId") Long newsId, @Param("isActivated") Boolean isActivated);

}
