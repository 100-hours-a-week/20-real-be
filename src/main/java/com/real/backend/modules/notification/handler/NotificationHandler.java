package com.real.backend.modules.notification.handler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.real.backend.modules.notification.dto.NoticeCreatedEvent;
import com.real.backend.modules.notification.service.NotificationSseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationSseService notificationSseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void noticeNotificationHandler(NoticeCreatedEvent event) {
        notificationSseService.sendNoticeNotification(event.getNotice());
    }
}
