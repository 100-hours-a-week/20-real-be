package com.real.backend.domain.notice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.service.NoticeService;
import com.real.backend.response.DataResponse;
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
    public DataResponse<SliceDTO<NoticeListResponseDTO>> getNoticeListByCursor(@RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @CurrentSession Session session) {

        SliceDTO<NoticeListResponseDTO> noticeList = noticeService.getNoticeListByCursor(cursorId, limit, cursorStandard, session.getId());
        return DataResponse.of(noticeList);
    }
}
