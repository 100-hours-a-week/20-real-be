package com.real.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.AiResponseService;

@TestConfiguration
public class AiResponseConfig {
    @Bean
    public AiResponseService aiResponseService() {
        return new AiResponseService() {
            @Override
            public <T, R> R postForAiResponse(String path, T requestDto, Class<R> responseType) {
                if (responseType.equals(NoticeSummaryResponseDTO.class)) {
                    return responseType.cast(new NoticeSummaryResponseDTO("요약", true));
                }
                throw new IllegalArgumentException("지원하지 않는 타입");
            }
        };
    }
}
