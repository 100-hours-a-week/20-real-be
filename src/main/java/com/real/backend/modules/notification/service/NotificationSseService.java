package com.real.backend.modules.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.common.util.CONSTANT;
import com.real.backend.infra.sse.repository.SseEmitterRepository;
import com.real.backend.modules.notification.dto.NotificationResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

    private final SseEmitterRepository sseEmitterRepository;

    public SseEmitter connect(Long userId) {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter sseEmitter = new SseEmitter(CONSTANT.ACCESS_TOKEN_EXPIRED);

        sseEmitter.onCompletion(() -> sseEmitterRepository.delete(userId, emitterId));
        sseEmitter.onTimeout(() -> sseEmitterRepository.delete(userId, emitterId));
        sseEmitter.onError((e) -> sseEmitterRepository.delete(userId, emitterId));

        sseEmitterRepository.save(userId, emitterId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE 연결 성공"));
        } catch (IOException e) {
            sseEmitterRepository.delete(userId, emitterId);
        }

        return sseEmitter;
    }

    public void sendNotification(Long userId, NotificationResponseDTO notificationResponseDTO) {
        Map<String, SseEmitter> userEmitters = sseEmitterRepository.get(userId);
        if (userEmitters == null) return;

        userEmitters.forEach((emitterId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notificationResponseDTO));
            } catch (IOException e) {
                sseEmitterRepository.delete(userId, emitterId);
            }
        });
    }

    public void sendNotificationToAll(NotificationResponseDTO notificationResponseDTO) {
        sseEmitterRepository.findAllEmitters().forEach((userId, emitters) -> {
            emitters.forEach((emitterId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notificationResponseDTO));
                } catch (IOException e) {
                    sseEmitterRepository.delete(userId, emitterId);
                }
            });
        });
    }
}
