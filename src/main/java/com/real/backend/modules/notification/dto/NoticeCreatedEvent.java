package com.real.backend.modules.notification.dto;

import com.real.backend.modules.notice.domain.Notice;

import lombok.Getter;

@Getter
public class NoticeCreatedEvent {
    private final Notice notice;

    public NoticeCreatedEvent(Notice notice) {
        this.notice = notice;
    }
}
