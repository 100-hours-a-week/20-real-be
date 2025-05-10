package com.real.backend.domain.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsCommentRequestDTO(
    @NotBlank
    @Size(min = 1, max = 500)
    String content
) {
}
