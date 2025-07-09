package com.real.backend.modules.notification.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.common.util.CONSTANT;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.infra.sse.repository.SseEmitterRepository;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.notification.domain.NotificationType;
import com.real.backend.modules.notification.dto.NotificationResponseDTO;
import com.real.backend.modules.notification.repository.NotificationRepository;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final NotificationRepository notificationRepository;
    private final NoticeRedisService noticeRedisService;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional
    public SseEmitter connect(Long userId, Long lastEventId) {
        SseEmitter sseEmitter = new SseEmitter(CONSTANT.CONNECTION_TIMEOUT);

        sseEmitter.onCompletion(() -> sseEmitterRepository.delete(userId));
        sseEmitter.onTimeout(() -> {
            sseEmitter.complete();
            sseEmitterRepository.delete(userId);
        });
        sseEmitter.onError((e) -> sseEmitterRepository.delete(userId));

        sseEmitterRepository.save(userId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE 연결 성공"));
        } catch (IOException e) {
            sseEmitterRepository.delete(userId);
        }

        recoverMissedNotification(userId, lastEventId, sseEmitter);
        recoverMissedNoticeNotification(userId, lastEventId, sseEmitter);

        return sseEmitter;
    }

    public void sendNotification(Long userId, NotificationResponseDTO notificationResponseDTO) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(notificationResponseDTO));
        } catch (IOException e) {
            sseEmitterRepository.delete(userId);
        }
    }

    public void sendNoticeNotification(Notice notice) {
        Map<Long, SseEmitter> emitters = sseEmitterRepository.findAllEmitters();
        emitters.forEach((userId, emitter) -> {
            Role role = userRepository.findRoleById(userId);
            if (!(role == Role.STAFF || role == Role.TRAINEE)) {
                return;
            }
            try {
                emitter.send(SseEmitter.event()
                    .id(notice.getId().toString())
                    .data(NotificationResponseDTO.builder()
                        .referenceId(notice.getId())
                        .type(NotificationType.NOTICE_CREATED)
                        .userId(userId)
                        .message("새로운 공지가 생성되었습니다.")
                        .build()));
            } catch (IOException e) {
                sseEmitterRepository.delete(userId);
            }
        });
    }

    public void sendNotificationToAll(NotificationResponseDTO notificationResponseDTO) {
        sseEmitterRepository.findAllEmitters().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notificationResponseDTO));
            } catch (IOException e) {
                sseEmitterRepository.delete(userId);
            }
        });
    }

    public void disconnect(Long userId) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;
        emitter.complete();
        sseEmitterRepository.delete(userId);
    }

    public void sendHeartbeat() {
        sseEmitterRepository.findAllEmitters().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat").data("heartbeat sended"));
            } catch (IOException e) {
                sseEmitterRepository.delete(userId);
            }
        });
    }

    @Transactional(readOnly = true)
    public void recoverMissedNotification(Long userId, Long lastEventID, SseEmitter emitter) {
        notificationRepository.findByUserIdAndIdGreaterThanOrderByIdAsc(userId, lastEventID)
            .forEach(notification -> {
                try {
                    NotificationResponseDTO dto = NotificationResponseDTO.from(notification);
                    emitter.send(SseEmitter.event()
                        .id(dto.getNotificationId().toString())
                        .name("notification")
                        .data(dto));
                } catch (IOException e) {
                    disconnect(userId);
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
                        .name("newNotice")
                        .data(NotificationResponseDTO.builder()
                            .type(NotificationType.NOTICE_CREATED)
                            .referenceId(notice.getId())
                            .userId(userId)
                            .message("새로운 공지가 생성되었습니다.")
                            .build()));
                } catch (IOException e) {
                    disconnect(userId);
                }
            });
    }
}
