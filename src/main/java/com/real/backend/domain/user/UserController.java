package com.real.backend.domain.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/v1/users/info")
    public DataResponse<?> getUserInfo(@CurrentSession Session session) {
        LoginResponseDTO loginResponseDTO = userService.getUserInfo(session.getId());

        return DataResponse.of(loginResponseDTO);
    }
}
