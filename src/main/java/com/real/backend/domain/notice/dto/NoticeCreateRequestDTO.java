package com.real.backend.domain.notice.dto;

public record NoticeCreateRequestDTO(
    String title,
    String content,
    String tag,
    String originalUrl
) {
}
