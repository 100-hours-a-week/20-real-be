package com.real.backend.modules.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.user.domain.UserNoticeRead;

@Repository
public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
    Boolean existsByUserIdAndNoticeId(Long userId, Long noticeId);
}
