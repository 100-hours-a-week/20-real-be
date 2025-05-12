package com.real.backend.infra.ai.dto;

import jakarta.validation.constraints.NotBlank;

@NotBlank
public record NoticeSummaryRequestDTO(
    String content,
    String title
) {
}
