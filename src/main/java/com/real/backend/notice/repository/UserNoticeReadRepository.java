package com.real.backend.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.notice.domain.UserNoticeRead;

public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
}
