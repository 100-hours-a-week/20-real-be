package com.real.backend.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.notice.domain.NoticeComment;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
}
