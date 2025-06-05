package com.real.backend.modules.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.domain.NoticeFile;

@Repository
public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    List<NoticeFile> findByNoticeOrderByFileSeqNoAsc(Notice notice);
}
