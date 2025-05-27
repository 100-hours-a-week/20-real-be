package com.real.backend.infra.ai.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.ai.dto.ChatbotRequestDTO;
import com.real.backend.infra.ai.dto.ChatbotResponseDataDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final AiResponseService aiResponseService;

    public ChatbotResponseDataDTO makeQuestion(ChatbotRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v1/chatbots",
            dto,
            ChatbotResponseDataDTO.class
        );
    }
}
