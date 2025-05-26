package com.real.backend.domain.news.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.news.dto.NewsCreateRequestDTO;
import com.real.backend.domain.news.dto.NewsListResponseDTO;
import com.real.backend.domain.news.service.NewsService;
import com.real.backend.domain.news.dto.NewsResponseDTO;
import com.real.backend.response.DataResponse;
import com.real.backend.response.StatusResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;
import com.real.backend.util.dto.SliceDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;

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
}
