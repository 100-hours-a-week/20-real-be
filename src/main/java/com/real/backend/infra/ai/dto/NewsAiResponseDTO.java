package com.real.backend.infra.ai.dto;

public record NewsAiResponseDTO(
    String headline,
    String summary,
    String content,
    Boolean isCompleted
) {
}
