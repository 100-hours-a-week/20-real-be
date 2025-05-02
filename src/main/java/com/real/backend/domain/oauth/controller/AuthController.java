package com.real.backend.domain.oauth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.oauth.kakao.KakaoUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AuthController {

    private final KakaoUtil kakaoUtil;

    @GetMapping("/v1/oauth/{provider}")
    public void oauthLogin(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        if (provider.equals("kakao")) {
            response.sendRedirect(kakaoUtil.redirectToKakaoLogin());
        }
    }
}
