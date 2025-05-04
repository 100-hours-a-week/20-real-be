package com.real.backend.domain.notice.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.notice.domain.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("""
        SELECT n FROM Notice n
        WHERE  n.deletedAt IS NULL
        ORDER  BY n.createdAt DESC, n.id DESC
        """)
    Slice<Notice> fetchLatestFirst(Pageable pg);   // 첫 페이지

    @Query("""
        SELECT n FROM Notice n
        WHERE  n.deletedAt IS NULL
          AND ( n.createdAt < :cAt
                OR (n.createdAt = :cAt AND n.id < :id) )
        ORDER BY n.createdAt DESC, n.id DESC
        """)
    Slice<Notice> fetchLatest(@Param("cAt") LocalDateTime cAt,
        @Param("id") Long id,
        Pageable pg);
}
