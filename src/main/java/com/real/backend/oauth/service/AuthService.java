package com.real.backend.oauth.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.real.backend.oauth.dto.KakaoProfileDTO;
import com.real.backend.oauth.dto.KakaoTokenDTO;
import com.real.backend.oauth.kakao.KakaoUtil;
import com.real.backend.security.JwtUtil;
import com.real.backend.user.domain.LoginType;
import com.real.backend.user.domain.Role;
import com.real.backend.user.domain.Status;
import com.real.backend.user.domain.User;
import com.real.backend.user.UserRepository;
import com.real.backend.util.CONSTANT;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final KakaoUtil kakaoUtil;
    private final JwtUtil jwtUtil;

    // TODO 자체 토큰 발급
    public User oAuthLogin(String accessCode, HttpServletResponse response) {
        KakaoTokenDTO oauthToken = kakaoUtil.getAccessToken(accessCode);
        KakaoProfileDTO kakaoProfile = kakaoUtil.getKakaoProfile(oauthToken);

        String email = kakaoProfile.getKakao_account().getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> createNewUser(kakaoProfile));

        String accessToken = jwtUtil.generateToken("access", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.ACCESS_TOKEN_EXPIRED);
        String refreshToken = jwtUtil.generateToken("refresh", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.REFRESH_TOKEN_EXPIRED);

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
            .httpOnly(true)
            .secure(true)               // HTTPS 필수
            .path("/")                  // 전 API
            .sameSite("Lax")            // CSRF 완화
            .maxAge(CONSTANT.ACCESS_TOKEN_EXPIRED)
            .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/auth/refresh")      // RT 사용 경로 한정
            .sameSite("None")           // 크로스-사이트 재발급용
            .maxAge(CONSTANT.REFRESH_TOKEN_EXPIRED)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return user;

    }

    // TODO 프로필 사진 s3 버킷 연결
    private User createNewUser(KakaoProfileDTO kakaoProfile) {
        User user = User.builder()
            .email(kakaoProfile.getKakao_account().getEmail())
            .nickname(kakaoProfile.getProperties().getNickname())
            // .profileUrl("")
            .loginType(LoginType.OAUTH)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.save(user);
        return user;
    }
}
