package com.real.backend.domain.news.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.news.domain.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    @Query("""
        SELECT n FROM News n
        WHERE  n.deletedAt IS NULL
        ORDER  BY n.createdAt DESC, n.id DESC
        """)
    Slice<News> fetchLatestFirst(Pageable pg);   // 첫 페이지

    @Query("""
        SELECT n FROM News n
        WHERE  n.deletedAt IS NULL
          AND ( n.createdAt < :cAt
                OR (n.createdAt = :cAt AND n.id < :id) )
        ORDER BY n.createdAt DESC, n.id DESC
        """)
    Slice<News> fetchLatest(@Param("cAt") LocalDateTime cAt,
        @Param("id") Long id,
        Pageable pg);

    /* 인기순 ------------------------------------------------------------ */
    @Query("""
        SELECT n FROM News n
        WHERE  n.deletedAt IS NULL
        ORDER  BY n.todayViewCount DESC, n.id DESC
        """)
    Slice<News> fetchPopularFirst(Pageable pg);

    @Query("""
        SELECT n FROM News n
        WHERE  n.deletedAt IS NULL
          AND ( n.todayViewCount < :views
                OR (n.todayViewCount = :views AND n.id < :id) )
        ORDER BY n.todayViewCount DESC, n.id DESC
        """)
    Slice<News> fetchPopular(@Param("views") Long views,
        @Param("id")    Long id,
        Pageable pg);

    @Modifying
    @Query("UPDATE News n SET n.todayViewCount = 0")
    void resetTodayViewCount();
}
