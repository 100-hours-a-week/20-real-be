package com.real.backend.modules.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.modules.user.dto.ChangeUserInfoRequestDTO;
import com.real.backend.modules.user.dto.ChangeUserRoleRequestDTO;
import com.real.backend.modules.user.dto.LoginResponseDTO;
import com.real.backend.modules.user.service.UserService;
import com.real.backend.common.response.DataResponse;
import com.real.backend.common.response.StatusResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

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

    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @PatchMapping("/v1/users/role")
    public StatusResponse changeRole(
        @RequestBody ChangeUserRoleRequestDTO changeUserRoleRequestDTO
    ) {
        userService.changeUserRole(changeUserRoleRequestDTO);

        return StatusResponse.of(200, "사용자 role이 정상적으로 수정되었습니다.");
    }

    @PatchMapping("/v1/users/enroll")
    public StatusResponse changeUserInfo(
        @RequestBody ChangeUserInfoRequestDTO changeUserInfoRequestDTO
    ) {
        userService.enrollUser(changeUserInfoRequestDTO);

        return StatusResponse.of(200, "사용자가 정상적으로 등록되었습니다.");
    }
}
