package com.real.backend.domain.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.domain.user.dto.UserUnreadNoticeResponseDTO;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.response.DataResponse;
import com.real.backend.response.StatusResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/v1/users/info")
    public DataResponse<?> getUserInfo(@CurrentSession Session session) {
        LoginResponseDTO loginResponseDTO = userService.getUserInfo(session.getId());

        return DataResponse.of(loginResponseDTO);
    }

    @GetMapping("v1/users/notices/unread")
    public DataResponse<?> getUnreadNotices(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @CurrentSession Session session) {
        SliceDTO<UserUnreadNoticeResponseDTO> userUnreadNoticeResponseDTOList = userService.getNoticeListByCursor(
            cursorId, limit, cursorStandard, session.getId());

        return DataResponse.of(userUnreadNoticeResponseDTOList);
    }

    @PostMapping("v1/users/notices/read")
    public StatusResponse readNotices(@CurrentSession Session session) {
        userService.readNotices(session.getId());
        return StatusResponse.of(200, "안 읽은 공지들이 성공적으로 읽음 처리 되었습니다.");
    }
}
