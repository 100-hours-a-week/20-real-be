package com.real.backend.common.interceptor;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.real.backend.security.JwtUtils;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiAccessInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }

        if (!uri.startsWith("/api/") || uri.startsWith("/api/v1/auth/") || uri.startsWith("/api/v2/auth/") || uri.startsWith("/api/v1/oauth/")
            || uri.startsWith("/api/v1/news/")) return true;

        String token = extractTokenFromCookie(request);
        if (token == null || !jwtUtils.validateToken(token, response)) return false;

        Long userId = jwtUtils.getId(token);
        if (userId != null) {
            String key = "user:" + "active:" + userId;
            redisTemplate.opsForValue().set(key, "active", 1, TimeUnit.MINUTES);
        }

        return true;
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
