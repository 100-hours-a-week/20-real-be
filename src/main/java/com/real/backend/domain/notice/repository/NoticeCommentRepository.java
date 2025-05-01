package com.real.backend.domain.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.domain.notice.domain.NoticeComment;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
}
