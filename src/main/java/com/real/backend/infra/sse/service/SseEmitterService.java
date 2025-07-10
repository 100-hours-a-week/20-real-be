package com.real.backend.infra.sse.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseEmitterService {

    public Long parseLastEventId(String lastEventIdHeader) {
        long lastEventId = 0L;
        if (lastEventIdHeader != null) {
            try {
                lastEventId = Long.parseLong(lastEventIdHeader);
            } catch (NumberFormatException ignored) {}
        }
        return lastEventId;
    }
}
