package com.real.backend.modules.notification.service;

import java.io.IOException;

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
        SseEmitter sseEmitter = new SseEmitter(CONSTANT.CONNECTION_TIMEOUT);

        sseEmitter.onCompletion(() -> sseEmitterRepository.delete(userId));
        sseEmitter.onTimeout(() -> sseEmitterRepository.delete(userId));
        sseEmitter.onError((e) -> sseEmitterRepository.delete(userId));

        sseEmitterRepository.save(userId, sseEmitter);

        try {
            sseEmitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE 연결 성공"));
        } catch (IOException e) {
            sseEmitterRepository.delete(userId);
        }

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
        Map<String, SseEmitter> userEmitters = sseEmitterRepository.get(userId);
        if (userEmitters == null) return;
        userEmitters.forEach((emitterId, emitter) -> {
            emitter.complete();
            sseEmitterRepository.delete(userId, emitterId);
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;
        emitter.complete();
        sseEmitterRepository.delete(userId);
    }
        });
    }
}
