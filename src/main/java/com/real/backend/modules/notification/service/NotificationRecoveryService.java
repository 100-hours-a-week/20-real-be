package com.real.backend.modules.notification.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.infra.sse.service.SseEmitterService;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.notification.domain.NotificationType;
import com.real.backend.modules.notification.dto.NotificationEventDTO;
import com.real.backend.modules.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationRecoveryService {
    private final NotificationRepository notificationRepository;
    private final NoticeRepository noticeRepository;
    private final SseEmitterService sseEmitterService;
    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    public void recoverMissedNotification(Long userId, Long lastEventID, SseEmitter emitter) {
        notificationRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, lastEventID)
            .forEach(notification -> {
                try {
                    NotificationEventDTO dto = NotificationEventDTO.from(notification);
                    emitter.send(SseEmitter.event()
                        .id(dto.getNotificationId().toString())
                        .name("notification")
                        .data(dto));
                } catch (IOException e) {
                    sseEmitterService.disconnect(userId);
                }
            });
    }

    @Transactional(readOnly = true)
    public void recoverMissedNoticeNotification(Long userId, Long lastEventID, SseEmitter emitter) {
        List<Long> readList = noticeRedisService.getUserReadList(userId);

        noticeRepository.findByIdGreaterThanOrderByIdAsc(lastEventID)
            .stream()
            .filter(notice -> !readList.contains(notice.getId()))
            .forEach(notice -> {
                try {
                    emitter.send(SseEmitter.event()
                        .id(notice.getId().toString())
                        .name("notice")
                        .data(NotificationEventDTO.builder()
                            .type(NotificationType.NOTICE_CREATED)
                            .referenceId(notice.getId())
                            .userId(userId)
                            .message("새로운 공지가 생성되었습니다.")
                            .build()));
                } catch (IOException e) {
                    sseEmitterService.disconnect(userId);
                }
            });
    }
}
