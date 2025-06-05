package com.real.backend.modules.news.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.news.dto.NewsLikeResponseDTO;
import com.real.backend.modules.news.service.NewsLikeService;
import com.real.backend.common.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NewsLikeController {

    private final NewsLikeService newsLikeService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PutMapping("/v1/news/{newsId}/likes")
    public DataResponse<NewsLikeResponseDTO> editNewsLike(@PathVariable Long newsId, @CurrentSession Session session) {
        Long userId = session.getId();

        NewsLikeResponseDTO newsLikeResponseDTO = newsLikeService.editNewsLike(newsId, userId);
        return DataResponse.of(newsLikeResponseDTO);
    }
}
