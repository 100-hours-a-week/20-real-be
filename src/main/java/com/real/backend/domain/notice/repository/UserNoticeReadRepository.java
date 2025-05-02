package com.real.backend.domain.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.domain.notice.domain.UserNoticeRead;

public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
}
