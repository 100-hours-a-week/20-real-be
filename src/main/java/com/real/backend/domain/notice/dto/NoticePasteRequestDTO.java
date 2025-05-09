package com.real.backend.domain.notice.dto;

public record NoticePasteRequestDTO(
    String title,
    String content,
    String tag,
    String originalUrl,
    String userName,
    String platform,
    String createdAt
) {
}
