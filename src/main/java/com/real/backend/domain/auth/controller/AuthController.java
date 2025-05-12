package com.real.backend.domain.auth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.auth.kakao.KakaoUtil;
import com.real.backend.domain.auth.service.AuthService;
import com.real.backend.response.StatusResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    private final KakaoUtil kakaoUtil;
    private final AuthService authService;

    @GetMapping("/v1/oauth/{provider}")
    public void oauthLogin(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        if (provider.equals("kakao")) {
            response.sendRedirect(kakaoUtil.redirectToKakaoLogin());
        }
    }

    @PostMapping("/v1/auth/logout")
    public StatusResponse logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return StatusResponse.of(204, "성공적으로 로그아웃 됐습니다.");
    }
}
