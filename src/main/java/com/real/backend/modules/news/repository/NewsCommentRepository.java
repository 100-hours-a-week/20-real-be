package com.real.backend.modules.news.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.news.domain.NewsComment;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {

	@Query("""
        SELECT n FROM NewsComment n
        WHERE  n.deletedAt IS NULL
          AND n.news.id = :newsId
        ORDER  BY n.createdAt DESC, n.id DESC
        """)
	Slice<NewsComment> fetchLatestFirst(Pageable pg, @Param("newsId") Long newsId);   // 첫 페이지

	@Query("""
        SELECT n FROM NewsComment n
        WHERE  n.deletedAt IS NULL
          AND n.news.id = :newsId
          AND ( n.createdAt < :cAt
                OR (n.createdAt = :cAt AND n.id < :id) )
        ORDER BY n.createdAt DESC, n.id DESC
        """)
	Slice<NewsComment> fetchLatest(@Param("cAt") LocalDateTime cAt,
		@Param("id") Long id,
		Pageable pg,
        @Param("newsId") Long newsId);

    @Modifying
    @Query("""
    UPDATE NewsComment n
    SET n.deletedAt = :deletedAt
    WHERE n.id = :newsId
"""
    )
    void deleteByNewsId(Long newsId, LocalDateTime deletedAt);
}
