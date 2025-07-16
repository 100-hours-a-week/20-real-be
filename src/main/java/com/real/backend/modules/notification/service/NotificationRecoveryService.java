package com.real.backend.modules.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.modules.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationRecoveryService {
    private final NoticeRepository noticeRepository;
    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    public boolean findLatestUnreadNotice(Long userId) {
        List<Long> readList = noticeRedisService.getUserReadList(userId);

        if (readList.isEmpty()) {
            return false;
        } else {
            return noticeRepository.findTopByIdGreaterThanAndIdNotInOrderByIdDesc(userId, readList).isPresent();
        }

        // return noticeRepository
        //     .findFirstByOrderByCreatedAtDesc()
        //     .map(notice -> !readList.contains(notice.getId()))
        //     .orElse(false);
    }
}
