package com.real.backend.modules.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.real.backend.infra.ai.dto.ChatbotRequestDTO;

import reactor.core.publisher.Flux;

@Service
public class ChatbotService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    @Value("${spring.api.secret}")
    private String apiKey;

    private final WebClient webClient;

    public ChatbotService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(aiUrl).build();
    }

    public Flux<ServerSentEvent<String>> streamAnswer(String question, Long userId) {
        ChatbotRequestDTO chatbotRequestDTO = ChatbotRequestDTO.of(question, userId);
        return webClient.post()
            .uri(aiUrl +"/api/v3/chatbots")
            .header("x-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(chatbotRequestDTO)
            .retrieve()
            .bodyToFlux(String.class)
            .map(chunk -> ServerSentEvent.builder(chunk).build())
            .concatWith(Flux.just(ServerSentEvent.builder("[DONE]").event("done").build()))
            .doOnError(err -> System.err.println("❌ SSE Error: " + err.getMessage()));
    }
}
