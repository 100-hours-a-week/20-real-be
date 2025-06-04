package com.real.backend.infra.ai.dto;

import com.real.backend.global.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotRequestDTO {
    @NotBlank
    @Sanitizer
    String question;

    Long userId;

    public static ChatbotRequestDTO of(String question, Long userId) {
        return ChatbotRequestDTO.builder()
            .question(question)
            .userId(userId)
            .build();
    }
}
