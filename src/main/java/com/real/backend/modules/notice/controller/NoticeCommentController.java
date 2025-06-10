package com.real.backend.modules.notice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.notice.dto.NoticeCommentRequestDTO;
import com.real.backend.modules.notice.dto.NoticeCommentListResponseDTO;
import com.real.backend.modules.notice.dto.NoticeStressResponseDTO;
import com.real.backend.modules.notice.service.NoticeCommentService;
import com.real.backend.modules.notice.service.NoticeService;
import com.real.backend.common.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;
import com.real.backend.common.util.dto.SliceDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeCommentController {
    private final NoticeCommentService noticeCommentService;
    private final NoticeService noticeService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("v1/notices/{noticeId}/comments")
    public DataResponse<?> getNoticeCommentListByCursor(@PathVariable Long noticeId,
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @CurrentSession Session session) {

        Long currentUserId = session.getId();
        SliceDTO<NoticeCommentListResponseDTO> noticeCommentList = noticeCommentService.getNoticeCommentListByCursor(noticeId,
            cursorId, cursorStandard, limit, currentUserId);

        return DataResponse.of(noticeCommentList);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @DeleteMapping("v1/notices/{noticeId}/comments/{commentId}")
    public DataResponse<NoticeStressResponseDTO> deleteNoticeComment(
        @PathVariable Long noticeId,
        @PathVariable Long commentId,
        @CurrentSession Session session
    ) {
        Long userId = session.getId();
        NoticeStressResponseDTO noticeStressResponseDTO = noticeCommentService.deleteNoticeComment(noticeId, commentId, userId);
        return DataResponse.of(noticeStressResponseDTO);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("v1/notices/{noticeId}/comments")
    public DataResponse<NoticeStressResponseDTO> createNoticeComment(
        @PathVariable Long noticeId,
        @CurrentSession Session session,
        @Valid @RequestBody NoticeCommentRequestDTO noticeCommentRequestDTO
    ) {
        Long userId = session.getId();
        NoticeStressResponseDTO noticeStressResponseDTO = noticeCommentService.createNoticeComment(noticeId, userId, noticeCommentRequestDTO);

        return DataResponse.of(noticeStressResponseDTO);
    }
}
