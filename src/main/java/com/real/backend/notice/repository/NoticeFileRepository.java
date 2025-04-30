package com.real.backend.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.real.backend.notice.domain.NoticeFile;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
}
