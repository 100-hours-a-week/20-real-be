package com.real.backend.infra.sse.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long userId, SseEmitter emitter) {
        return emitters.put(userId, emitter);
    }

    public SseEmitter get(Long userId) {
        return emitters.get(userId);
    }

    public void delete(Long userId, SseEmitter emitter) {
        emitters.remove(userId, emitter);
    }


    public Map<Long, SseEmitter> findAllEmitters() {
        return emitters;
    }
}
