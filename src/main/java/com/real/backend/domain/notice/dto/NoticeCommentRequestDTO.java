package com.real.backend.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCommentRequestDTO(
	@NotBlank
	@Size(min = 1, max = 500)
    String content
) {
}
