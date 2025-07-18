package com.real.backend.common.util;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtils {

    @Value("${spring.cookie_domain}")
    private String domain;

    private final boolean isSecure = true;
    private final boolean isHttpOnly = true;


    public String resolveTokenFromCookie(HttpServletRequest request, String type) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
            .filter(c -> type.equals(c.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
    }

    public void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = createResponseCookie(CONSTANT.ACCESS_TOKEN_COOKIE, accessToken, isHttpOnly, isSecure, "/",
            "Lax");
        ResponseCookie refreshCookie = createResponseCookie(CONSTANT.REFRESH_TOKEN_COOKIE, refreshToken, isHttpOnly, isSecure,
            "/api/v1/auth", "None");

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void deleteTokenCookies(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = deleteResponseCookie(CONSTANT.ACCESS_TOKEN_COOKIE,  isHttpOnly, isSecure, "/",
            "Lax");
        ResponseCookie deleteRefreshCookie = deleteResponseCookie(CONSTANT.REFRESH_TOKEN_COOKIE,  isHttpOnly, isSecure,
            "/api/v1/auth", "None");

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

    }

    public ResponseCookie createResponseCookie(String name, String token, boolean isHttpOnly, boolean isSecure, String path, String sameSite) {
        return ResponseCookie.from(name, token)
            .httpOnly(isHttpOnly)
            .secure(isSecure)
            .path(path) // RT 사용 경로 한정
            .sameSite(sameSite) // 크로스-사이트 재발급용
            .maxAge(CONSTANT.REFRESH_TOKEN_EXPIRED)
            .domain(domain)
            .build();
    }

    public ResponseCookie deleteResponseCookie(String name, boolean isHttpOnly, boolean isSecure, String path, String sameSite) {
        return ResponseCookie.from(name, "")
            .httpOnly(isHttpOnly)
            .secure(isSecure)
            .path(path)
            .sameSite(sameSite)
            .maxAge(0)
            .domain(domain)
            .build();
    }

}
