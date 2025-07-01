package com.real.backend.modules.auth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.common.response.StatusResponse;
import com.real.backend.common.util.CookieUtils;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.auth.kakao.KakaoUtil;
import com.real.backend.modules.auth.service.TokenService;
import com.real.backend.modules.notification.service.NotificationSseService;
import com.real.backend.security.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {
    private final KakaoUtil kakaoUtil;
    private final CookieUtils cookieUtils;
    private final TokenService tokenService;
    private final NotificationSseService notificationSseService;
    private final JwtUtils jwtUtils;

    @GetMapping("/v1/oauth/{provider}")
    public void oauthLogin(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        if (provider.equals("kakao")) {
            response.sendRedirect(kakaoUtil.redirectToKakaoLogin());
        }
    }

    @PostMapping("/v1/auth/logout")
    public StatusResponse logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.deleteRefreshToken(cookieUtils.resolveTokenFromCookie(request, "REFRESH_TOKEN"));
        notificationSseService.disconnect(jwtUtils.getId(cookieUtils.resolveTokenFromCookie(request, "ACCESS_TOKEN")));
        cookieUtils.deleteTokenCookies(response);
        return StatusResponse.of(204, "성공적으로 로그아웃 됐습니다.");
    }

    @PostMapping("/v1/auth/refresh")
    public StatusResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        TokenDTO tokenDTO = tokenService.refreshAccessToken(cookieUtils.resolveTokenFromCookie(request, "REFRESH_TOKEN"));
        cookieUtils.setTokenCookies(response, tokenDTO.accessToken(), tokenDTO.refreshToken());
        return StatusResponse.of(200, "OK");
    }
}
