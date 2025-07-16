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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private final SseEmitterRepository sseEmitterRepository;
    private final UserRepository userRepository;
    private final NotificationRecoveryService notificationRecoveryService;

    public SseEmitter connect(Long userId, Long lastEventId) {
        SseEmitter newEmitter = new SseEmitter(CONSTANT.CONNECTION_TIMEOUT);

        newEmitter.onCompletion(() -> {
            sseEmitterRepository.delete(userId, newEmitter);
        });
        newEmitter.onTimeout(() -> {
            log.info("User {} is Timed out. Closing the connection.", userId);
            newEmitter.complete();
        });
        newEmitter.onError(e -> {
            log.info("User {} is Error. Closing the connection. Reason: ", userId, e);
            newEmitter.complete();
        });

        SseEmitter oldEmitter = sseEmitterRepository.save(userId, newEmitter);
        if (oldEmitter != null) {
            log.warn("User {} is reconnecting. Closing the previous connection.", userId);
            oldEmitter.complete();
        }

        log.info("User {} is connected successfully", userId);

        try {
            newEmitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE connection established."));
        } catch (IOException e) {
            log.warn("Failed to send initial connect event for user {}. The client may have disconnected. Completing emitter.", userId, e);
            newEmitter.complete();
        }

        if (notificationRecoveryService.findLatestUnreadNotice(userId)) {
            sendNotification(userId, NotificationEventDTO.builder()
                .type(NotificationType.NOTICE_CREATED)
                .message("새로운 공지가 생성되었습니다.")
                .userId(userId)
                .build());
        }

        return newEmitter;
    }

    public void sendNotification(Long userId, NotificationEventDTO notificationEventDTO) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(notificationEventDTO));
        } catch (IOException e) {
            emitter.complete();
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
                emitter.complete();
            }
        });
    }

    public void sendHeartbeat() {
        sseEmitterRepository.findAllEmitters().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .comment("heartbeat"));
            } catch (IOException | IllegalStateException e) {
                sseEmitterRepository.delete(userId, emitter);
            }
        });
    }
}
