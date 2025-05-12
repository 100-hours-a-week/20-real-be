package com.real.backend.domain.oauth.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.oauth.service.AuthService;
import com.real.backend.domain.user.domain.User;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class KakaoController {
    private final AuthService authService;
    @Value("${spring.server_url}")
    private String serverUrl;

    @GetMapping("/v1/oauth/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) throws
		IOException {
        User user = authService.oAuthLogin(accessCode, response);
        response.sendRedirect(serverUrl + "/login/success");
    }

}
