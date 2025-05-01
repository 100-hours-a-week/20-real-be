package com.real.backend.domain.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.domain.notice.domain.NoticeFile;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
}
