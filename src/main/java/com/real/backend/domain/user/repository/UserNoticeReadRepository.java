package com.real.backend.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.user.domain.UserNoticeRead;
import com.real.backend.domain.user.domain.User;

@Repository
public interface UserNoticeReadRepository extends JpaRepository<UserNoticeRead, Long> {
    Optional<UserNoticeRead> findByUserAndNotice(User user, Notice notice);
}
