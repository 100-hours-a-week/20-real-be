package com.real.backend.domain.notice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeLike;
import com.real.backend.domain.user.domain.User;

@Repository
public interface NoticeLikeRepository extends JpaRepository<NoticeLike, Long> {

    Optional<NoticeLike> findByNoticeAndUser(Notice notice, User user);
}
