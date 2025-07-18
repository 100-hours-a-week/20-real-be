package com.real.backend.modules.news.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.news.dto.NewsCommentRequestDTO;
import com.real.backend.modules.news.dto.NewsStressResponseDTO;
import com.real.backend.modules.news.service.NewsService;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.news.dto.NewsCommentListResponseDTO;
import com.real.backend.modules.news.service.NewsCommentService;
import com.real.backend.common.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsCommentController {

    private final NewsCommentService newsCommentService;
    private final NewsService newsService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("v1/news/{newsId}/comments")
    public DataResponse<?> getNewsCommentListByCursor(@PathVariable Long newsId,
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @CurrentSession Session session) {

        Long currentUserId = session.getId();
        SliceDTO<NewsCommentListResponseDTO> newsCommentList = newsCommentService.getNewsCommentListByCursor(newsId,
            cursorId, cursorStandard, limit, currentUserId);

        return DataResponse.of(newsCommentList);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @DeleteMapping("v1/news/{newsId}/comments/{commentId}")
    public DataResponse<NewsStressResponseDTO> deleteNewsComment(@PathVariable Long newsId, @PathVariable Long commentId, @CurrentSession Session session) {

        Long userId = session.getId();
        NewsStressResponseDTO newsStressResponseDTO = newsCommentService.deleteNewsComment(newsId, commentId, userId);
        return DataResponse.of(newsStressResponseDTO);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("v1/news/{newsId}/comments")
    public DataResponse<NewsStressResponseDTO> createNewsComment(@PathVariable Long newsId,
        @CurrentSession Session session,
        @Valid @RequestBody NewsCommentRequestDTO newsCommentRequestDTO
    ) {
        Long userId = session.getId();
        NewsStressResponseDTO newsStressResponseDTO = newsCommentService.createNewsComment(newsId, userId, newsCommentRequestDTO);

        return DataResponse.of(newsStressResponseDTO);
    }
}
