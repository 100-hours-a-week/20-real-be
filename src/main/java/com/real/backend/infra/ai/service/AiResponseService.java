package com.real.backend.infra.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.infra.s3.S3FileInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseService {
    @Value("${spring.ai_url}")
    private String aiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public <T, R> R postForAiResponse(String path, T requestDto, Class<R> responseType) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestDto, headers);

        int maxAttempts = 3;
        int attempt = 0;
        ResponseEntity<String> response = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                response = restTemplate.exchange(
                    aiUrl + path,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
                );

                if (response.getStatusCode().is5xxServerError()) {
                    if (attempt < maxAttempts) {
                        continue;
                    } else {
                        throw new ResponseStatusException(
                            response.getStatusCode(),
                            "FastAPI 서버 오류로 요청 실패 (재시도 " + attempt + "회): " + response.getBody()
                        );
                    }
                }

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new ResponseStatusException(
                        response.getStatusCode(),
                        "FastAPI 호출 실패: status=" + response.getStatusCode() + ", body=" + response.getBody()
                    );
                }

                // 정상 응답
                JsonNode body = objectMapper.readTree(response.getBody());
                JsonNode dataNode = body.path("data");
                return objectMapper.treeToValue(dataNode, responseType);

            } catch (RestClientException e) {
                if (attempt >= maxAttempts) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "FastAPI 호출 실패 (RestClientException)", e);
                }
                // log.info("AI 호출에 실패했습니다.");
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FastAPI 호출 재시도 실패");
    }


    public S3FileInfoResponse getS3FileInfo(String path) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

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

        return objectMapper.treeToValue(dataNode, S3FileInfoResponse.class);
    }
}
