package com.real.backend.domain.news.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.news.dto.NewsCommentRequestDTO;
import com.real.backend.response.StatusResponse;
import com.real.backend.util.dto.SliceDTO;
import com.real.backend.domain.news.dto.NewsCommentListResponseDTO;
import com.real.backend.domain.news.service.NewsCommentService;
import com.real.backend.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsCommentController {

    private final NewsCommentService newsCommentService;

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
    public StatusResponse deleteNewsComment(@PathVariable Long newsId, @PathVariable Long commentId, @CurrentSession Session session) {

        Long userId = session.getId();
        newsCommentService.deleteNewsComment(newsId, commentId, userId);
        return StatusResponse.of(204, "댓글이 정상적으로 삭제됐습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("v1/news/{newsId}/comments")
    public StatusResponse createNewsComment(@PathVariable Long newsId,
        @CurrentSession Session session,
        @RequestBody NewsCommentRequestDTO newsCommentRequestDTO) {
        Long userId = session.getId();
        newsCommentService.createNewsComment(newsId, userId, newsCommentRequestDTO);

        return StatusResponse.of(200, "댓글이 성공적으로 생성되었습니다.");
    }
}
