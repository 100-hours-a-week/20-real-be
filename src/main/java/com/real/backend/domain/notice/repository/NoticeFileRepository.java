package com.real.backend.domain.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeFile;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    List<NoticeFile> findByNoticeOrderByFileSeqNoAsc(Notice notice);
}
