package com.real.backend.infra.sse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.real.backend.infra.sse.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {
    private final SseEmitterRepository sseEmitterRepository;

    public Long parseLastEventId(String lastEventIdHeader) {
        long lastEventId = 0L;
        if (lastEventIdHeader != null) {
            try {
                lastEventId = Long.parseLong(lastEventIdHeader);
            } catch (NumberFormatException ignored) {}
        }
        return lastEventId;
    }

    public void disconnect(Long userId) {
        SseEmitter emitter = sseEmitterRepository.get(userId);
        if (emitter == null) return;
        emitter.complete();
    }
}
