package com.real.backend.modules.notice.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.notice.domain.NoticeComment;

@Repository
public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
    @Query("""
        SELECT n FROM NoticeComment n
        WHERE  n.deletedAt IS NULL
          AND n.notice.id = :noticeId
        ORDER  BY n.createdAt DESC, n.id DESC
        """)
    Slice<NoticeComment> fetchLatestFirst(Pageable pg, @Param("noticeId") Long noticeId);   // 첫 페이지

    @Query("""
        SELECT n FROM NoticeComment n
        WHERE  n.deletedAt IS NULL
          AND n.notice.id = :noticeId
          AND ( n.createdAt < :cAt
                OR (n.createdAt = :cAt AND n.id < :id) )
        ORDER BY n.createdAt DESC, n.id DESC
        """)
    Slice<NoticeComment> fetchLatest(@Param("cAt") LocalDateTime cAt,
        @Param("id") Long id,
        Pageable pg,
        @Param("noticeId") Long noticeId);

    @Modifying
    @Query("""
    UPDATE NoticeComment n
    SET n.deletedAt = :deletedAt
    WHERE n.id = :noticeId
"""
    )
    void deleteByNoticeId(Long noticeId, LocalDateTime deletedAt);
}
