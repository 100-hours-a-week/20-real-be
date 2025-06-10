package com.real.backend.modules.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.modules.user.service.UserInvitedService;
import com.real.backend.common.response.StatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserInvitedController {

    private final UserInvitedService userInvitedService;

    @PostMapping("/v1/invited-user")
    public StatusResponse createInvitedUser(
        @RequestBody InvitedUserCreateRequestDTO invitedUserCreateRequestDTO
    ) {
        userInvitedService.createInvitedUser(invitedUserCreateRequestDTO);

        return StatusResponse.of(201, "초대 유저가 정상적으로 생성되었습니다.");
    }
}
