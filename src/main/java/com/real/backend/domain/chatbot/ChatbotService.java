package com.real.backend.domain.chatbot;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.ai.dto.ChatbotRequestDTO;
import com.real.backend.infra.ai.dto.ChatbotResponseDataDTO;
import com.real.backend.infra.ai.service.AiResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final AiResponseService aiResponseService;

    public ChatbotResponseDataDTO makeQuestion(ChatbotRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v2/chatbots",
            dto,
            ChatbotResponseDataDTO.class
        );
    }
}
