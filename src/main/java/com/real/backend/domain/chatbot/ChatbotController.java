package com.real.backend.domain.chatbot;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.ai.dto.ChatbotRequestDTO;
import com.real.backend.infra.ai.dto.ChatbotResponseDataDTO;
import com.real.backend.infra.ai.service.ChatbotService;
import com.real.backend.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("/v1/chatbots")
    public DataResponse<ChatbotResponseDataDTO> makeQuestion(
        @Valid @RequestBody ChatbotRequestDTO chatbotRequestDTO,
        @CurrentSession Session session
        ) throws
        JsonProcessingException {
        chatbotRequestDTO.setUserId(session.getId());
        ChatbotResponseDataDTO chatbotResponsedataDTO = chatbotService.makeQuestion(chatbotRequestDTO);

        return DataResponse.of(chatbotResponsedataDTO);
    }
}
