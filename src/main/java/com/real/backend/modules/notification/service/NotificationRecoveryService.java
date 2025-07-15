package com.real.backend.modules.notification.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationRecoveryService {
    private final NoticeRepository noticeRepository;
    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    public Optional<Notice> findLatestUnreadNotice(Long userId, Long lastEventID) {
        List<Long> readList = noticeRedisService.getUserReadList(userId);

        return noticeRepository
            .findByIdGreaterThanOrderByIdDesc(lastEventID)
            .stream()
            .filter(notice -> !readList.contains(notice.getId()))
            .findFirst();
    }
}
