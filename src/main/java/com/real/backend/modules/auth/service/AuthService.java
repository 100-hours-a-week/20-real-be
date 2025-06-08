package com.real.backend.modules.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.UnauthorizedException;
import com.real.backend.common.util.CONSTANT;
import com.real.backend.common.util.CookieUtils;
import com.real.backend.modules.auth.domain.RefreshToken;
import com.real.backend.modules.auth.dto.KakaoProfileDTO;
import com.real.backend.modules.auth.dto.KakaoTokenDTO;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.auth.kakao.KakaoUtil;
import com.real.backend.modules.auth.repository.RefreshTokenRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;
import com.real.backend.modules.user.service.UserSignupService;
import com.real.backend.security.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserSignupService userSignupService;
    private final KakaoUtil kakaoUtil;
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    private final boolean isSecure = true;
    private final boolean isHttpOnly = true;
    private final UserFinder userFinder;
    private final TokenService tokenService;

    @Transactional
    protected void generateToken(HttpServletResponse response, User user) {
        String accessToken = jwtUtils.generateToken("access", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.ACCESS_TOKEN_EXPIRED);

        LocalDateTime expiryTime = LocalDateTime.now().plus(Duration.ofSeconds(CONSTANT.REFRESH_TOKEN_EXPIRED));
        String refreshToken = jwtUtils.generateToken("refresh", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.REFRESH_TOKEN_EXPIRED);

        ResponseCookie accessCookie = cookieUtils.createResponseCookie("ACCESS_TOKEN", accessToken, isHttpOnly, isSecure, "/",
            "Lax");
        ResponseCookie refreshCookie = cookieUtils.createResponseCookie("REFRESH_TOKEN", refreshToken, isHttpOnly, isSecure,
            "/api/v1/auth", "None");

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryTime(expiryTime)
            .build());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    @Transactional
    public TokenDTO oAuthLogin(String accessCode) {
        KakaoTokenDTO oauthToken = kakaoUtil.getAccessToken(accessCode);
        KakaoProfileDTO kakaoProfile = kakaoUtil.getKakaoProfile(oauthToken);

        String email = kakaoProfile.getKakao_account().getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> userSignupService.createOAuthUser(kakaoProfile));

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return new TokenDTO(accessToken, refreshToken);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.resolveTokenFromCookie(request, "REFRESH_TOKEN");

        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        ResponseCookie deleteAccessCookie = cookieUtils.deleteResponseCookie("ACCESS_TOKEN",  isHttpOnly, isSecure, "/",
            "Lax");
        ResponseCookie deleteRefreshCookie = cookieUtils.deleteResponseCookie("REFRESH_TOKEN",  isHttpOnly, isSecure,
            "/api/v1/auth", "None");

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

    }

    @Transactional
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.resolveTokenFromCookie(request, "REFRESH_TOKEN");

        if (refreshToken == null) {
            throw new UnauthorizedException("refresh token is null");
        }
        User user = userFinder.getUser(jwtUtils.getId(refreshToken));
        RefreshToken saved = refreshTokenRepository.findByUser(user).orElseThrow(() -> new UnauthorizedException("refresh token not found"));

        if (!saved.getToken().equals(refreshToken)) {
            throw new UnauthorizedException("refresh token does not match");
        }

        generateToken(response, user);
    }
}
