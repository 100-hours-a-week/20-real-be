package com.real.backend.domain.notice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeLike;
import com.real.backend.domain.user.domain.User;

@Repository
public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {

    Optional<NoticeLike> findByNoticeAndUser(Notice notice, User user);

    @Modifying
    @Query(value = """
    INSERT INTO notice_like (user_id, notice_id, is_activated)
    VALUES (:userId, :noticeId, true)
    ON DUPLICATE KEY UPDATE
    is_activated = NOT is_activated
""", nativeQuery = true)
    void insertOrToggle(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

}
