package com.real.backend.modules.notice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.real.backend.modules.notice.domain.Notice;

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

    /**
     * 첫 페이지: userId가 읽지 않은(=UserNoticeRead 레코드가 없는) 최신 Notice(limit+1개)
     */
    @Query("""
        SELECT n
          FROM Notice n
         WHERE n.deletedAt IS NULL
           AND NOT EXISTS (
               SELECT 1
                 FROM UserNoticeRead unr
                WHERE unr.notice = n
                  AND unr.user.id = :userId
           )
         ORDER BY n.createdAt DESC, n.id DESC
        """)
    Slice<Notice> fetchUnreadLatestFirst(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * 다음 페이지: cursor 이후 userId가 읽지 않은(=UserNoticeRead 레코드가 없는) Notice(limit+1개)
     */
    @Query("""
        SELECT n
          FROM Notice n
         WHERE n.deletedAt IS NULL
           AND NOT EXISTS (
               SELECT 1
                 FROM UserNoticeRead unr
                WHERE unr.notice = n
                  AND unr.user.id = :userId
           )
           AND (
               n.createdAt < :cAt
            OR (n.createdAt = :cAt AND n.id < :id)
           )
         ORDER BY n.createdAt DESC, n.id DESC
        """)
    Slice<Notice> fetchUnreadLatest(
        @Param("cAt")       LocalDateTime cAt,
        @Param("id")        Long cursorId,
        @Param("userId")    Long userId,
        Pageable pageable
    );

    @Query("select n from Notice n " +
        "where n.deletedAt is null and (n.title like %:keyword% or n.content like %:keyword%) " +
        "order by n.createdAt desc, n.id desc")
    Slice<Notice> searchByKeywordFirst(@Param("keyword") String keyword, Pageable pageable);

    @Query("select n from Notice n " +
        "where n.deletedAt is null and (n.title like %:keyword% or n.content like %:keyword%) " +
        "and (n.createdAt < :cursorStandard or (n.createdAt = :cursorStandard and n.id < :cursorId)) " +
        "order by n.createdAt desc, n.id desc")
    Slice<Notice> searchByKeyword(@Param("keyword") String keyword, @Param("cursorStandard") LocalDateTime cursorStandard, @Param("cursorId") Long cursorId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notice n SET n.totalViewCount = :totalView, n.likeCount = :likeCount, n.commentCount = :commentCount WHERE n.id = :id")
    void updateCounts(Long id, Long totalView, Long likeCount, Long commentCount);

    @Query("select n.id from Notice n")
    List<Long> findAllNoticeIds();

    Optional<Notice> findTopByDeletedAtIsNullOrderByIdDesc();
}
