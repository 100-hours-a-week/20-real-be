package com.real.backend.infra.ai.dto;

public record NewsAiResponseDTO(
    String headline,
    String summary,
    String news,
    // String imageUrl,
    Boolean isCompleted
) {
}
