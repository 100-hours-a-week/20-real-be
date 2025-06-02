package com.real.backend.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.real.backend.security.JwtUtil;
import com.real.backend.security.Session;
import com.real.backend.util.CookieUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtils cookieUtils;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 건너 뛰어야 하는 경로 건너뛰기
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cookieUtils.resolveTokenFromCookie(request, "ACCESS_TOKEN");

        if (!jwtUtil.validateToken(token, response)){
            return;
        }

        // 토큰에서 정보 획득
        Long id = jwtUtil.getId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // 매 요청마다 ContextHolder에 Authentication 추가
        Session session = new Session(id, username, role);
        Authentication authToken = new UsernamePasswordAuthenticationToken(session, null, session.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken); // user session 생성

        // TODO 블랙리스트 확인 절차

        // 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        List<String> skipURI = Arrays.asList("/login", "/auth/.*", "/users/signup", "/api/v1/oauth/.*", "/api/v1/news", "/api/healthz", "/error", "/api/v1/auth/refresh","/monitoring/health","/monitoring/info","/monitoring/prometheus");
        return skipURI.stream().anyMatch(uri -> {
            Pattern pattern = Pattern.compile(uri);
            return pattern.matcher(request.getRequestURI()).matches();
        });
    }

}
