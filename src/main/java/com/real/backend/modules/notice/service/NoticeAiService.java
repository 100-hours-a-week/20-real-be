package com.real.backend.modules.notice.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.AiResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeAiService {
    private final AiResponseService aiResponseService;

    public NoticeSummaryResponseDTO makeSummary(NoticeSummaryRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v1/notices/summarization",
            dto,
            NoticeSummaryResponseDTO.class
        );
    }
}
