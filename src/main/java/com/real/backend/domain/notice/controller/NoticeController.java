package com.real.backend.domain.notice.controller;

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
import com.real.backend.domain.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.domain.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.service.NoticeService;
import com.real.backend.domain.notice.dto.NoticePasteRequestDTO;
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
public class NoticeController {
    private final NoticeService noticeService;

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

    // TODO 파일 받는 로직
    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/notices")
    public StatusResponse createNotice(
        @CurrentSession Session session,
        @Valid @RequestBody NoticeCreateRequestDTO noticeCreateRequestDTO
    ) throws JsonProcessingException {
        noticeService.createNotice(session.getId(), noticeCreateRequestDTO);
        return StatusResponse.of(201, "공지가 성공적으로 생성되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/notices/{noticeId}")
    public DataResponse<NoticeInfoResponseDTO> getNoticeById(
        @PathVariable Long noticeId,
        @CurrentSession Session session
    ) {
        noticeService.userReadNotice(noticeId, session.getId());
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
        @Valid @RequestBody NoticePasteRequestDTO noticePasteRequestDTO,
        @CurrentSession Session session
    ) {
        noticeService.editNotice(noticeId, noticePasteRequestDTO);
        return StatusResponse.of(200, "공지가 성공적으로 수정되었습니다.");
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PostMapping("/v1/notices/tmp")
    public StatusResponse pasteNoticeTmp(
        @Valid @RequestPart("notice") NoticePasteRequestDTO noticePasteRequestDTO,
        @RequestPart(value = "images", required = false) List<MultipartFile> images,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws JsonProcessingException {
        noticeService.pasteNoticeTmp(noticePasteRequestDTO, images, files);
        return StatusResponse.of(201, "공지가 성공적으로 생성되었습니다.");
    }
}
