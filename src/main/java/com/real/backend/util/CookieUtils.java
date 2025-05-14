package com.real.backend.util;

import java.util.Arrays;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {
    public String resolveTokenFromCookie(HttpServletRequest request, String type) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
            .filter(c -> type.equals(c.getName()))
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
            .maxAge(CONSTANT.REFRESH_TOKEN_EXPIRED)
            .build();
    }

    public ResponseCookie deleteResponseCookie(String name, boolean isHttpOnly, boolean isSecure, String path, String sameSite) {
        return ResponseCookie.from(name, "")
            .httpOnly(isHttpOnly)
            .secure(isSecure)
            .path(path)
            .sameSite(sameSite)
            .maxAge(0)
            .build();
    }

}
