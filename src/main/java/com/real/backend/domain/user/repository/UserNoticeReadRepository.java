package com.real.backend.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.user.domain.UserNoticeRead;
import com.real.backend.domain.user.domain.User;

@Repository
public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
    Optional<UserNoticeRead> findByUserAndNotice(User user, Notice notice);

    @Modifying
    @Transactional
    @Query(
        value = "INSERT IGNORE INTO user_notice_read (user_id, notice_id) VALUES (:userId, :noticeId)",
        nativeQuery = true
    )
    void insertIgnore(@Param("userId") Long userId,
        @Param("noticeId") Long noticeId);
}
