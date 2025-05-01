package com.real.backend.news;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.news.dto.NewsResponseDTO;
import com.real.backend.news.dto.NewsSliceDTO;
import com.real.backend.response.DataResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/v1/news")
    public DataResponse<NewsSliceDTO> getAllNews(@RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam("sort") String sort) {

        NewsSliceDTO newsList = newsService.getAllNews(cursorId, limit, sort, cursorStandard);
        return DataResponse.of(newsList);
    }

    @GetMapping("/v1/news/{newsId}")
    public DataResponse<NewsResponseDTO> getNewsById(@PathVariable("newsId") Long newsId) {
        newsService.increaseViewCounts(newsId);
        NewsResponseDTO newsResponseDTO = newsService.getNews(newsId);

        return DataResponse.of(newsResponseDTO);
    }
}
