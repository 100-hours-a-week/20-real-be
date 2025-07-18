package com.real.backend.modules.notice.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.response.DataResponse;
import com.real.backend.common.response.StatusResponse;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.modules.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.modules.notice.dto.NoticeListResponseDTO;
import com.real.backend.modules.notice.service.NoticeCursorPaginationService;
import com.real.backend.modules.notice.service.NoticeService;
import com.real.backend.modules.notification.service.NotificationSseService;
import com.real.backend.modules.user.service.UserNoticeService;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeController {
    private final UserNoticeService userNoticeService;
    private final NoticeService noticeService;
    private final NotificationSseService notificationSseService;
    private final NoticeCursorPaginationService noticeCursorPaginationService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/notices")
    public DataResponse<SliceDTO<NoticeListResponseDTO>> getNoticeListByCursor(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @CurrentSession Session session
    ) {
        SliceDTO<NoticeListResponseDTO> noticeList = noticeService.getNoticeListByCursor(cursorId, limit,
            cursorStandard, session.getId());
        return DataResponse.of(noticeList);
    }

    // keyword를 이용한 공지 목록 검색
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v2/notices")
    public DataResponse<SliceDTO<NoticeListResponseDTO>> getNoticeListByCursor(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam(value = "keyword", required = false) String keyword,
        @CurrentSession Session session
    ) {
        SliceDTO<NoticeListResponseDTO> noticeList = noticeCursorPaginationService.getNoticeListByCursor(cursorId, limit,
            cursorStandard, session.getId(), keyword);
        return DataResponse.of(noticeList);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/notices/{noticeId}")
    public DataResponse<NoticeInfoResponseDTO> getNoticeById(
        @PathVariable Long noticeId,
        @CurrentSession Session session
    ) {
        userNoticeService.userReadNotice(noticeId, session.getId());
        NoticeInfoResponseDTO noticeInfoResponseDTO = noticeService.getNoticeById(noticeId, session.getId());

        return DataResponse.of(noticeInfoResponseDTO);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @DeleteMapping("/v1/notices/{noticeId}")
    public StatusResponse deleteNotice(
        @PathVariable Long noticeId,
        @CurrentSession Session session
    ) {
        noticeService.deleteNotice(noticeId);
        return StatusResponse.of(204, "공지가 성공적으로 삭제되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PutMapping("/v1/notices/{noticeId}")
    public StatusResponse editNotice(
        @PathVariable Long noticeId,
        @Valid @RequestBody NoticeCreateRequestDTO noticeCreateRequestDTO,
        @CurrentSession Session session
    ) {
        noticeService.editNotice(noticeId, noticeCreateRequestDTO);
        return StatusResponse.of(200, "공지가 성공적으로 수정되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/notices")
    public StatusResponse createNotice(
        @Valid @RequestPart("notice") NoticeCreateRequestDTO noticeCreateRequestDTO,
        @RequestPart(value = "images", required = false) List<MultipartFile> images,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws JsonProcessingException {
        noticeService.createNotice(noticeCreateRequestDTO, images, files);
        return StatusResponse.of(201, "공지가 성공적으로 생성되었습니다.");
    }
}
