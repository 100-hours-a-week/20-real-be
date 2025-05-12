package com.real.backend.domain.notice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.notice.dto.NoticeCommentRequestDTO;
import com.real.backend.domain.notice.dto.NoticeCommentListResponseDTO;
import com.real.backend.domain.notice.service.NoticeCommentService;
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
public class NoticeCommentController {
    private final NoticeCommentService noticeCommentService;

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
    public StatusResponse deleteNoticeComment(
        @PathVariable Long noticeId,
        @PathVariable Long commentId,
        @CurrentSession Session session
    ) {
        Long userId = session.getId();
        noticeCommentService.deleteNoticeComment(noticeId, commentId, userId);
        return StatusResponse.of(204, "댓글이 정상적으로 삭제됐습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("v1/notices/{noticeId}/comments")
    public StatusResponse createNoticeComment(
        @PathVariable Long noticeId,
        @CurrentSession Session session,
        @Valid @RequestBody NoticeCommentRequestDTO noticeCommentRequestDTO
    ) {
        Long userId = session.getId();
        noticeCommentService.createNoticeComment(noticeId, userId, noticeCommentRequestDTO);

        return StatusResponse.of(200, "댓글이 성공적으로 생성되었습니다.");
    }
}
