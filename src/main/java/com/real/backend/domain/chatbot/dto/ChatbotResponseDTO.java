package com.real.backend.domain.chatbot.dto;

public record ChatbotResponseDTO(
    String message,
    ChatbotResponseDataDTO data
) {}
