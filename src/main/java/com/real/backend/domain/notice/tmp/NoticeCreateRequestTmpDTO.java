package com.real.backend.domain.notice.tmp;

public record NoticeCreateRequestTmpDTO(
    String title,
    String content,
    String tag,
    String originalUrl,
    String userName,
    String platform
) {
}
