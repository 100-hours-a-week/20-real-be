package com.real.backend.modules.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.notification.domain.Notification;
import com.real.backend.modules.notification.domain.NotificationType;
import com.real.backend.modules.notification.dto.NotificationResponseDTO;
import com.real.backend.modules.notification.repository.NotificationRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserFinder userFinder;
    private final NotificationRepository notificationRepository;
    private final NotificationSseService notificationSseService;

    @Transactional
    public void createAndSendNotification(Long userId, Long referenceId, NotificationType notificationType, String message) {
        User user = userFinder.getUser(userId);
        Notification notification = Notification.builder()
            .user(user)
            .referenceId(referenceId)
            .message(message)
            .type(notificationType)
            .build();
        notificationRepository.save(notification);
        notificationSseService.sendNotification(userId, NotificationResponseDTO.from(notification));
    }

}
