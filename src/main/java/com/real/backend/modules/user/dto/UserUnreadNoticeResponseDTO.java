package com.real.backend.modules.user.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UserUnreadNoticeResponseDTO(
    Long id,
    String title,
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    LocalDateTime createdAt
) {
}
