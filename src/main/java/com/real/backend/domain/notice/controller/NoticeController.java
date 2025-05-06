package com.real.backend.domain.notice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.domain.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.service.NoticeService;
import com.real.backend.response.DataResponse;
import com.real.backend.response.StatusResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeController {
    private final NoticeService noticeService;

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
    @PostMapping("/v1/notices")
    public StatusResponse createNotice(
        @CurrentSession Session session,
        @RequestBody NoticeCreateRequestDTO noticeCreateRequestDTO
    ) {
        noticeService.createNotice(session.getId(), noticeCreateRequestDTO);
        return StatusResponse.of(201, "공지가 성공적으로 생성되었습니다.");
    }

    @GetMapping("/v1/notices/{noticeId}")
    public DataResponse<NoticeInfoResponseDTO> getNoticeById(
        @PathVariable Long noticeId,
        @CurrentSession Session session
    ) {
        noticeService.increaseViewCounts(noticeId);
        noticeService.userReadNotice(noticeId, session.getId());
        NoticeInfoResponseDTO noticeInfoResponseDTO = noticeService.getNoticeById(noticeId, session.getId());

        return DataResponse.of(noticeInfoResponseDTO);
    }
}
