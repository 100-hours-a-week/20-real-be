package com.real.backend.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequestDTO(
	@NotBlank
    @Size(min = 1, max = 26)
	String title,

	@NotBlank
	@Size(min = 1, max = 20000)
    String content,

	@NotBlank
    String tag,

    String originalUrl
) {
}
