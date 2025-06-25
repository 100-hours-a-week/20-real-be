package com.real.backend.modules.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.user.domain.UserNoticeRead;

@Repository
public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
    Boolean existsByUserIdAndNoticeId(Long userId, Long noticeId);

    @Query("""
    SELECT unr FROM UserNoticeRead unr
    JOIN unr.user u
    WHERE u.lastLoginAt >= :since
""")
    List<UserNoticeRead> findAllByUserLastLoginAtAfter(@Param("since") LocalDateTime since);

    @Query("SELECT notice.id from UserNoticeRead WHERE user.id = :userId")
    List<Long> findAllByUserId(@Param("userId") Long userId);
}
