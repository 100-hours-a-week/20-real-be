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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiResponseService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public <T, R> R postForAiResponse(String path, T requestDto, Class<R> responseType) throws
        JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            aiUrl + path,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(
                response.getStatusCode(),
                "FastAPI 호출 실패: status=" + response.getStatusCode() + ", body=" + response.getBody()
            );
        }

        JsonNode body = objectMapper.readTree(response.getBody());
        JsonNode dataNode = body.path("data");


        return objectMapper.treeToValue(dataNode, responseType);
    }
}
