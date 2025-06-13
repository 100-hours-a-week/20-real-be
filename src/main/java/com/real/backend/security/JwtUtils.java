package com.real.backend.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtils {

    private final SecretKey secretKey;

    // secret key 가져오기
    public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // 토큰 발급
    public String generateToken(String type, Long id, String username, String role, Long expiredSec) {
        long expiredMs = expiredSec * 1000;
        return Jwts.builder()
                .claim("type", type)
                .claim("id", id)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // id 가져오기
    public Long getId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    // 토큰 유저 이름 추출
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    // role 가져오기
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 발행시간 가져오기
    public long getIssuedAt(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getIssuedAt().getTime();
    }

    // type 가져오기
    public String getType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("type", String.class);
    }

    // 토큰 만료 확인
    private boolean isExpired(String token) throws MalformedJwtException {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, HttpServletResponse response) throws IOException {
        if (token == null) {
            // log.info("token is null");
            setBody(response, 401, "MISSING_TOKEN");
            return false;
        }

        try {
            if (isExpired(token)) {
                // log.info("token is expired");
                setBody(response, 401, "EXPIRED_TOKEN");
                return false;
            }
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException exception) {
            setBody(response, 401, "INVALID_TOKEN");
            return false;
        }
        return true;
    }

    private void setBody(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        String jsonResponse = String.format("{\"code\": %d, \"message\": \"%s\"}", code, message);
        response.getWriter().write(jsonResponse);
    }
}
