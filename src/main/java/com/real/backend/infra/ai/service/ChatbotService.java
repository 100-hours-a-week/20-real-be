package com.real.backend.infra.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.infra.ai.dto.ChatbotRequestDTO;
import com.real.backend.infra.ai.dto.ChatbotResponseDataDTO;

import reactor.core.publisher.Flux;

@Service
public class ChatbotService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    private final WebClient webClient;

    public ChatbotService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(aiUrl).build(); // FastAPI URL
    }

    public ChatbotResponseDataDTO makeQuestion(ChatbotRequestDTO chatbotRequestDTO) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청
        HttpEntity<ChatbotRequestDTO> requestEntity = new HttpEntity<>(chatbotRequestDTO, headers);
        ResponseEntity<String> response = restTemplate.exchange(aiUrl+"/api/v2/chatbots", HttpMethod.POST, requestEntity, String.class);

        if (! response.getStatusCode().is2xxSuccessful()) {

            throw new ResponseStatusException(
                response.getStatusCode(),
                "FastAPI 호출 실패: status=" + response.getStatusCode() + ", body=" + response.getBody()
            );
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode body = objectMapper.readTree(response.getBody());

        JsonNode dataNode = body.path("data");

        ChatbotResponseDataDTO data = objectMapper.treeToValue(dataNode, ChatbotResponseDataDTO.class);

        return new ChatbotResponseDataDTO(data.answer());
    }

    public Flux<ServerSentEvent<String>> streamAnswer(String question, Long userId) {
        ChatbotRequestDTO chatbotRequestDTO = ChatbotRequestDTO.of(question, userId);
        return webClient.post()
            .uri(aiUrl +"/api/v3/chatbots")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(chatbotRequestDTO)
            .retrieve()
            .bodyToFlux(String.class)
            .map(chunk -> ServerSentEvent.builder(chunk).build())
            .doOnError(err -> System.err.println("❌ SSE Error: " + err.getMessage()));
    }
}
