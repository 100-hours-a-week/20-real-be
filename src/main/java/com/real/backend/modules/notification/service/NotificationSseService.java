package com.real.backend.modules.notification.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.common.util.CONSTANT;
import com.real.backend.infra.sse.repository.SseEmitterRepository;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notification.domain.NotificationType;
import com.real.backend.modules.notification.dto.NotificationEventDTO;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final UserRepository userRepository;
    private final NotificationRecoveryService notificationRecoveryService;

    public SseEmitter connect(Long userId, Long lastEventId) {
        if (sseEmitterRepository.isExist(userId)) {
            sseEmitterRepository.get(userId).complete();
            sseEmitterRepository.delete(userId);
        }

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

        notificationRecoveryService.findLatestUnreadNotice(userId, lastEventId).ifPresent(this::sendNoticeNotification);

        return sseEmitter;
    }

    public void sendNotification(Long userId, NotificationEventDTO notificationEventDTO) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(notificationEventDTO));
        } catch (IOException e) {
            sseEmitterRepository.delete(userId);
        }
    }

    public void sendNoticeNotification(Notice notice) {
        List<Long> allUserIds = sseEmitterRepository.findAllEmitters().keySet().stream().toList();
        if (allUserIds.isEmpty()) {
            return;
        }
        Map<Long, Role> userRoles = userRepository.findAllById(allUserIds).stream()
            .collect(Collectors.toMap(User::getId, User::getRole));

        sseEmitterRepository.findAllEmitters().forEach((userId, emitter) -> {
            Role role = userRoles.get(userId);
            if (!(role == Role.STAFF || role == Role.TRAINEE)) {
                return;
            }

            try {
                emitter.send(SseEmitter.event()
                    .id(notice.getId().toString())
                    .name("notice")
                    .data(NotificationEventDTO.builder()
                        .referenceId(notice.getId())
                        .type(NotificationType.NOTICE_CREATED)
                        .userId(userId)
                        .message("새로운 공지가 생성되었습니다.")
                        .build()));
            } catch (Exception e) {
                emitter.complete();
            }
        });
    }

    public void sendNotificationToAll(NotificationEventDTO notificationEventDTO) {
        sseEmitterRepository.findAllEmitters().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notificationEventDTO));
            } catch (IOException e) {
                sseEmitterRepository.delete(userId);
            }
        });
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
}
