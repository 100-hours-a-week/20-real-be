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
    public boolean hasUnreadLatestNotice(Long userId) {
        List<Long> readList = noticeRedisService.getUserReadList(userId);

        Optional<Notice> latestOpt = noticeRepository
            .findTopByDeletedAtIsNullOrderByIdDesc();

        if (latestOpt.isEmpty()) {
            return false;
        }

        Long latestId = latestOpt.get().getId();
        return !readList.contains(latestId);
    }
}
