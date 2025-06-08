package com.real.backend.modules.auth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.common.util.CookieUtils;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.auth.kakao.KakaoUtil;
import com.real.backend.modules.auth.service.AuthService;
import com.real.backend.common.response.StatusResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final KakaoUtil kakaoUtil;
    private final CookieUtils cookieUtils;

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

    @PostMapping("v1/auth/refresh")
    public StatusResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        TokenDTO tokenDTO = authService.refreshAccessToken(cookieUtils.resolveTokenFromCookie(request, "REFRESH_TOKEN"));
        cookieUtils.setTokenCookie(response, tokenDTO.accessToken(), tokenDTO.refreshToken());
        return StatusResponse.of(200, "OK");
    }
}
