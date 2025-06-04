package com.real.backend.domain.chatbot;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.ai.dto.ChatbotRequestDTO;
import com.real.backend.infra.ai.dto.ChatbotResponseDataDTO;
import com.real.backend.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

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

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping(value = "/v2/chatbots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChatResponse(
        @RequestParam String question,
        @CurrentSession Session session
    ) {

        return chatbotService.streamAnswer(question, session.getId());
    }
}
