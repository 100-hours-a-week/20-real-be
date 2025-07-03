package com.real.backend.modules.notification.dto;

import com.real.backend.modules.notification.domain.Notification;
import com.real.backend.modules.notification.domain.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long notificationId;
    private Long userId;
    private NotificationType type;
    private Long referenceId;
    private String message;
    private boolean isRead;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
            .notificationId(notification.getId())
            .userId(notification.getUser().getId())
            .type(notification.getType())
            .referenceId(notification.getReferenceId())
            .message(notification.getMessage())
            .isRead(notification.isRead())
            .build();
    }
}
