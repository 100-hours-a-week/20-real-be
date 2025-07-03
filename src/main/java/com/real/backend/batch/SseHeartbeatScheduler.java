package com.real.backend.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.real.backend.common.util.CONSTANT;
import com.real.backend.modules.notification.service.NotificationSseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final NotificationSseService notificationSseService;

    @Scheduled(fixedRate = CONSTANT.HEARTBEAT_INTERVAL)
    public void heartbeat() {
        notificationSseService.sendHeartbeat();
    }
}
