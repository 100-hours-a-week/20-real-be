package com.real.backend.modules.chatbot.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.chatbot.service.ChatbotService;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping(value = "/v2/chatbots", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChatResponse(
        @RequestParam String question,
        @CurrentSession Session session
    ) {

        return chatbotService.streamAnswer(question, session.getId());
    }
}
