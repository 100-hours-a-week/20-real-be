package com.real.backend.domain.news.service;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.repository.NewsRepository;
import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.service.WikiService;
import com.real.backend.infra.ai.dto.NewsAiRequestDTO;
import com.real.backend.infra.ai.dto.NewsAiResponseDTO;
import com.real.backend.infra.ai.dto.WikiAiRequestDTO;
import com.real.backend.infra.ai.service.AiResponseService;
import com.real.backend.infra.s3.S3FileInfoResponse;
import com.real.backend.util.S3Utils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsAiService {
    private final AiResponseService aiResponseService;
    private final WikiService wikiService;
    private final NewsRepository newsRepository;
    private final S3Utils s3Utils;

    private final int RECENTLY_UPDATED_DATE_STANDARD = 1;
    private final String DIR_NAME = "static/news/ai/images";

    public NewsAiResponseDTO makeTitleAndSummary(NewsAiRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v1/news",
            dto,
            NewsAiResponseDTO.class
        );
    }

    public NewsAiResponseDTO makeNews(WikiAiRequestDTO dto) throws JsonProcessingException {
        return aiResponseService.postForAiResponse(
            "/api/v2/news",
            dto,
            NewsAiResponseDTO.class
        );
    }

    @Transactional
    public void createNewsAi() throws JsonProcessingException {
        List<Long> ids = wikiService.getRecentlyUpdatedWikiIdList(RECENTLY_UPDATED_DATE_STANDARD);
        Wiki wiki = wikiService.getRandomWiki(ids);

        S3FileInfoResponse s3FileInfoResponse = aiResponseService.getS3FileInfo("/api/v1/presigned");
        String key = s3Utils.generateKey(DIR_NAME, s3FileInfoResponse.getFileName());
        String url = s3Utils.generatePresignedUrl(DIR_NAME, s3FileInfoResponse.getFileName(), Duration.ofMinutes(5), s3FileInfoResponse.getContentType());

        WikiAiRequestDTO wikiAiRequestDTO = WikiAiRequestDTO.from(wiki, url);
        NewsAiResponseDTO newsAiResponseDTO = makeNews(wikiAiRequestDTO);

        String cloudFrontUrl = s3Utils.buildCloudFrontUrl(key);

        newsRepository.save(News.of(newsAiResponseDTO, cloudFrontUrl));
    }
}
