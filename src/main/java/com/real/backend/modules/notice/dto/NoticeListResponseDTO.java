package com.real.backend.modules.notice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.modules.notice.domain.Notice;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NoticeListResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String tag;
    private String platform;
    private Boolean userRead;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static NoticeListResponseDTO from(Notice notice, Boolean userRead, String author) {
        return NoticeListResponseDTO.builder()
            .id(notice.getId())
            .title(notice.getTitle())
            .author(author)
            .tag(notice.getTag())
            .platform(notice.getPlatform())
            .userRead(userRead)
            .createdAt(notice.getCreatedAt())
            .build();
    }
}
