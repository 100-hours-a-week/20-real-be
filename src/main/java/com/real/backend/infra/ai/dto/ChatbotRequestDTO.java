package com.real.backend.infra.ai.dto;

import com.real.backend.global.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;


public class ChatbotRequestDTO {
    @NotBlank
    @Sanitizer
    String question;
}
