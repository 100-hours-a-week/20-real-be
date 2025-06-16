package com.real.backend.modules.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.common.response.DataResponse;
import com.real.backend.common.response.StatusResponse;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.user.dto.UserUnreadNoticeResponseDTO;
import com.real.backend.modules.user.service.UserNoticeService;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserNoticeController {

    private final UserNoticeService userNoticeService;

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/users/notices/unread")
    public DataResponse<?> getUnreadNotices(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
        @CurrentSession Session session) {
        SliceDTO<UserUnreadNoticeResponseDTO> userUnreadNoticeResponseDTOList = userNoticeService.getNoticeListByCursor(
            cursorId, limit, cursorStandard, session.getId());

        return DataResponse.of(userUnreadNoticeResponseDTOList);
    }

    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("/v1/users/notices/read")
    public StatusResponse readNotices(@CurrentSession Session session) {
        userNoticeService.readAllNotice(session.getId());
        return StatusResponse.of(200, "안 읽은 공지들이 성공적으로 읽음 처리 되었습니다.");
    }
}
