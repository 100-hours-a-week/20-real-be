package com.real.backend.modules.notice.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeFinder {
    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public Notice getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow(() -> new NotFoundException("해당 id를 가진 공지가 존재하지 않습니다."));
        if (notice.getDeletedAt() != null) {
            throw new NotFoundException("해당 id를 가진 공지가 존재하지 않습니다.");
        }
        return notice;
    }
}
