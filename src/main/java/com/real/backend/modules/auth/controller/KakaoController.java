package com.real.backend.modules.auth.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.common.util.CookieUtils;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.auth.service.AuthService;
import com.real.backend.security.JwtUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class KakaoController {
    private final AuthService authService;
    private final CookieUtils cookieUtils;
    private final JwtUtils jwtUtils;
    @Value("${spring.server_url}")
    private String serverUrl;

    @GetMapping("/v1/oauth/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response)
        throws IOException {
        TokenDTO tokenDTO = authService.oAuthLogin(accessCode);
        cookieUtils.setTokenCookie(response, tokenDTO.accessToken(), tokenDTO.refreshToken());
        response.sendRedirect(serverUrl + "/login/success");
    }
}
