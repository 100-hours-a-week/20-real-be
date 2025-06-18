package com.real.backend.modules.news.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.common.exception.ServerException;
import com.real.backend.common.util.S3Utils;
import com.real.backend.infra.ai.dto.NewsAiRequestDTO;
import com.real.backend.infra.ai.dto.NewsAiResponseDTO;
import com.real.backend.infra.ai.dto.WikiAiRequestDTO;
import com.real.backend.infra.ai.dto.WikiNewsAiResponseDTO;
import com.real.backend.infra.ai.service.AiResponseService;
import com.real.backend.modules.news.domain.News;
import com.real.backend.modules.news.repository.NewsRepository;
import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAiService {
    private final AiResponseService aiResponseService;
    private final NewsRepository newsRepository;
    private final S3Utils s3Utils;

    private final String DIR_NAME = "static/news/ai/images";
    private final WikiRepository wikiRepository;

    public NewsAiResponseDTO makeTitleAndSummary(NewsAiRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v1/news",
            dto,
            NewsAiResponseDTO.class
        );
    }

    public WikiNewsAiResponseDTO makeNews(WikiAiRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v2/news",
            dto,
            WikiNewsAiResponseDTO.class
        );
    }

    @Transactional
    public void createNewsAiByRandomWiki() throws JsonProcessingException {
        List<Long> ids = wikiRepository.getAllIdOrderByUpdatedAtLimit(5);
        Long randomId = ids.get((int) (Math.random() * ids.size()));

        createNewsWithAi(randomId);
    }

    @Transactional
    public void createNewsAiByWikiId(Long wikiId) throws JsonProcessingException {
        createNewsWithAi(wikiId);
    }

    @Transactional
    public void createNewsWithAi(Long wikiId) throws JsonProcessingException {
        Wiki wiki = wikiRepository.findById(wikiId).orElseThrow(() -> new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다."));

        // S3FileInfoResponse s3FileInfoResponse = aiResponseService.getS3FileInfo("/api/v1/presigned");
        // String key = s3Utils.generateKey(DIR_NAME, s3FileInfoResponse.getFileName());
        // String url = s3Utils.generatePresignedUrl(DIR_NAME, s3FileInfoResponse.getFileName(), Duration.ofMinutes(5), s3FileInfoResponse.getContentType());


        String key = s3Utils.generateKey(DIR_NAME, "ai_gen.png");
        String url = s3Utils.generatePresignedUrl(key, Duration.ofMinutes(5), "image/png");

        WikiAiRequestDTO wikiAiRequestDTO = WikiAiRequestDTO.from(wiki, url);
        WikiNewsAiResponseDTO wikiNewsAiResponseDTO = null;
        for (int i = 0; i < 3; i++) {
            wikiNewsAiResponseDTO = makeNews(wikiAiRequestDTO);
            if (wikiNewsAiResponseDTO.getIsCompleted())
                break;
            }
        if (!wikiNewsAiResponseDTO.getIsCompleted()) {
            throw new ServerException("ai가 응답을 주지 못했습니다.");
        }

        String cloudFrontUrl = s3Utils.buildCloudFrontUrl(key);

        newsRepository.save(News.of(wikiNewsAiResponseDTO, cloudFrontUrl));
    }
}
