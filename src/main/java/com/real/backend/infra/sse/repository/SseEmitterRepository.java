package com.real.backend.infra.sse.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long userId, String emitterId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(emitterId, emitter);
        return emitter;
    }

    public Map<String, SseEmitter> get(Long userId) {
        return emitters.get(userId);
    }

    public void delete(Long userId, String emitterId) {
        Map<String, SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitterId);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }

    public Map<Long, Map<String, SseEmitter>> findAllEmitters() {
        return emitters;
    }
}
