package com.real.backend.modules.notice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.notice.dto.NoticeLikeResponseDTO;
import com.real.backend.modules.notice.service.NoticeLikeService;
import com.real.backend.common.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeLikeController {

    private final NoticeLikeService noticeLikeService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PutMapping("/v1/notices/{noticeId}/likes")
    public DataResponse<NoticeLikeResponseDTO> editNoticeLike(@PathVariable Long noticeId, @CurrentSession Session session) {
        Long userId = session.getId();

        NoticeLikeResponseDTO noticeLikeResponseDTO = noticeLikeService.editNoticeLike(noticeId, userId);
        return DataResponse.of(noticeLikeResponseDTO);
    }
}
