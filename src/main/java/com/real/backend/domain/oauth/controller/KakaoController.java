package com.real.backend.domain.oauth.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.oauth.service.AuthService;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.response.DataResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class KakaoController {
    private final AuthService authService;

    @GetMapping("/v1/oauth/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) throws
		IOException {
        User user = authService.oAuthLogin(accessCode, response);
        response.sendRedirect("http://localhost:3000/login/success");
    }

}
