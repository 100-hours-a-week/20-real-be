package com.real.backend.util;

import java.util.Arrays;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {
    public String resolveTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
            .filter(c -> "ACCESS_TOKEN".equals(c.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);
    }

    public ResponseCookie createResponseCookie(String name, String token, boolean isHttpOnly, boolean isSecure, String path, String sameSite) {
        return ResponseCookie.from(name, token)
            .httpOnly(isHttpOnly)
            .secure(isSecure)
            .path(path) // RT 사용 경로 한정
            .sameSite(sameSite) // 크로스-사이트 재발급용
            .maxAge(name.equals("REFRESH_TOKEN") ? CONSTANT.REFRESH_TOKEN_EXPIRED : CONSTANT.ACCESS_TOKEN_EXPIRED)
            .build();
    }
}
