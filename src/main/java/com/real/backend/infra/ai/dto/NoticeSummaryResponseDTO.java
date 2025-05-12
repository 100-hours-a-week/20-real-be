package com.real.backend.infra.ai.dto;

public record NoticeSummaryResponseDTO(
    String summary,
    Boolean isCompleted
) {
}
