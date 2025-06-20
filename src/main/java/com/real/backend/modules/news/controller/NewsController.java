package com.real.backend.modules.news.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.response.DataResponse;
import com.real.backend.common.response.StatusResponse;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.news.dto.NewsCreateRequestDTO;
import com.real.backend.modules.news.dto.NewsListResponseDTO;
import com.real.backend.modules.news.service.NewsAiService;
import com.real.backend.modules.news.service.NewsService;
import com.real.backend.modules.news.dto.NewsResponseDTO;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;
    private final NewsAiService newsAiService;

    @GetMapping("/v1/news")
    public DataResponse<SliceDTO<NewsListResponseDTO>> getNewsListByCursor(@RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam("sort") String sort) {

        SliceDTO<NewsListResponseDTO> newsList = newsService.getNewsListByCursor(cursorId, limit, sort, cursorStandard);
        return DataResponse.of(newsList);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/news/{newsId}")
    public DataResponse<NewsResponseDTO> getNewsById(@PathVariable("newsId") Long newsId, @CurrentSession Session session) {
        NewsResponseDTO newsResponseDTO = newsService.getNewsWithUserLiked(newsId, session.getId());

        return DataResponse.of(newsResponseDTO);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/news")
    public StatusResponse createNews(
        @Valid @RequestPart("news") NewsCreateRequestDTO newsCreateRequestDTO,
        @RequestPart(value = "image", required = false) MultipartFile image) throws JsonProcessingException {

        newsService.createNews(newsCreateRequestDTO, image);
        return StatusResponse.of(201, "뉴스가 성공적으로 생성되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/news/{wikiId}")
    public StatusResponse createWikiNewsById(@PathVariable Long wikiId) throws JsonProcessingException {
        newsAiService.createNewsAiByWikiId(wikiId);
        return StatusResponse.of(201, "뉴스가 성공적으로 생성되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @DeleteMapping("/v1/news/{newsId}")
    public StatusResponse deleteNews(
    @PathVariable Long newsId
    ) {
        newsService.deleteNews(newsId);
        return StatusResponse.of(204, "뉴스가 성공적으로 삭제되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/news/ai")
    public StatusResponse createNewsWithAi() throws JsonProcessingException {
        newsAiService.createNewsAiByRandomWiki();
        return StatusResponse.of(201, "뉴스가 성공적으로 생성되었습니다.");
    }
}
