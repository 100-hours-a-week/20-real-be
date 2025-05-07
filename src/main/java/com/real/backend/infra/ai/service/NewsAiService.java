package com.real.backend.infra.ai.service;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.infra.ai.dto.NewsAiRequestDTO;
import com.real.backend.infra.ai.dto.NewsAiResponseDTO;

@Service
public class NewsAiService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    public NewsAiResponseDTO makeTitleAndSummary(NewsAiRequestDTO newsAiRequestDTO) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청
        HttpEntity<NewsAiRequestDTO> requestEntity = new HttpEntity<>(newsAiRequestDTO, headers);
        ResponseEntity<String> response = restTemplate.exchange(aiUrl+"/api/v1/news", HttpMethod.POST, requestEntity, String.class);

        if (! response.getStatusCode().is2xxSuccessful()) {

            throw new ResponseStatusException(
                response.getStatusCode(),
                "FastAPI 호출 실패: status=" + response.getStatusCode() + ", body=" + response.getBody()
            );
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode body = objectMapper.readTree(response.getBody());

        JsonNode dataNode = body.path("data");

        NewsAiResponseDTO data = objectMapper.treeToValue(dataNode, NewsAiResponseDTO.class);
        return data;
    }
}
