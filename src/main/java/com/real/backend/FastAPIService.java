package com.real.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.domain.chatbot.dto.ChatbotRequestDTO;
import com.real.backend.domain.chatbot.dto.ChatbotResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FastAPIService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    public ChatbotResponseDTO makeQuestion(ChatbotRequestDTO chatbotRequestDTO) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청
        HttpEntity<ChatbotRequestDTO> requestEntity = new HttpEntity<>(chatbotRequestDTO, headers);
        ResponseEntity<String> response = restTemplate.exchange(aiUrl, HttpMethod.POST, requestEntity, String.class);

        if (! response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(
                response.getStatusCode(),
                "FastAPI 호출 실패: status=" + response.getStatusCode() + ", body=" + response.getBody()
            );
        }
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(response.getBody(), ChatbotResponseDTO.class);
    }
}
