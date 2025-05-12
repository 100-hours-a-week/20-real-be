package com.real.backend.domain.oauth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.oauth.domain.RefreshToken;
import com.real.backend.domain.oauth.dto.KakaoProfileDTO;
import com.real.backend.domain.oauth.dto.KakaoTokenDTO;
import com.real.backend.domain.oauth.kakao.KakaoUtil;
import com.real.backend.domain.oauth.repository.RefreshTokenRepository;
import com.real.backend.security.JwtUtil;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.domain.user.service.UserSignupService;
import com.real.backend.util.CONSTANT;
import com.real.backend.util.CookieUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserSignupService userSignupService;
    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;
    private final CookieUtils cookieUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    private final boolean isSecure = true;
    private final boolean isHttpOnly = true;

    @Transactional
    public User oAuthLogin(String accessCode, HttpServletResponse response) {
        KakaoTokenDTO oauthToken = kakaoUtil.getAccessToken(accessCode);
        KakaoProfileDTO kakaoProfile = kakaoUtil.getKakaoProfile(oauthToken);

        String email = kakaoProfile.getKakao_account().getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> userSignupService.createOAuthUser(kakaoProfile));
        String accessToken = jwtUtil.generateToken("access", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.ACCESS_TOKEN_EXPIRED);

        LocalDateTime expiryTime = LocalDateTime.now().plus(Duration.ofSeconds(CONSTANT.REFRESH_TOKEN_EXPIRED));
        String refreshToken = jwtUtil.generateToken("refresh", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.REFRESH_TOKEN_EXPIRED);


        ResponseCookie accessCookie = cookieUtils.createResponseCookie("ACCESS_TOKEN", accessToken, isHttpOnly, isSecure, "/",
            "Lax");
        ResponseCookie refreshCookie = cookieUtils.createResponseCookie("REFRESH_TOKEN", refreshToken, isHttpOnly, isSecure,
            "/api/v1/auth/refresh", "None");

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryTime(expiryTime)
            .build());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return user;

    }

}
