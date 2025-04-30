package com.real.backend.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.backend.notice.domain.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
