package com.real.backend.oauth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.oauth.service.AuthService;
import com.real.backend.response.DataResponse;
import com.real.backend.user.domain.User;
import com.real.backend.user.dto.LoginResponseDTO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class KakaoController {
    private final AuthService authService;

    @GetMapping("/v1/oauth/kakao/callback")
    public DataResponse<LoginResponseDTO> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) {
        User user = authService.oAuthLogin(accessCode, response);
        return DataResponse.of(LoginResponseDTO.builder()
            .nickname(user.getNickname())
            .role(user.getRole())
            .profileUrl(user.getProfileUrl())
            .build());
    }

}
