package com.real.backend.domain.news.service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsAiService {
    private final AiResponseService aiResponseService;
    private final WikiService wikiService;
    private final NewsRepository newsRepository;

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
        List<Long> ids = wikiService.getRecentlyUpdatedWikiIdList(1);
        Wiki wiki = wikiService.getRandomWiki(ids);
        WikiAiRequestDTO wikiAiRequestDTO = WikiAiRequestDTO.from(wiki);
        NewsAiResponseDTO newsAiResponseDTO = makeNews(wikiAiRequestDTO);

        newsRepository.save(News.of(newsAiResponseDTO, ""));
    }


}
