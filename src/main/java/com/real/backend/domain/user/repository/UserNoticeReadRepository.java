package com.real.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.user.domain.UserNoticeRead;

@Repository
public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
    Boolean existsByUserIdAndNoticeId(Long userId, Long noticeId);
}
