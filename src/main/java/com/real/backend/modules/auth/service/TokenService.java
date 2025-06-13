package com.real.backend.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.UnauthorizedException;
import com.real.backend.common.util.CONSTANT;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtUtils jwtUtils;
    private final UserFinder userFinder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public TokenDTO refreshAccessToken(String refreshToken) {
        if (refreshToken == null) { throw new UnauthorizedException("refresh token is null"); }
        String refreshKey = "refresh:" + hashToken(refreshToken);

        User user = userFinder.getUser(jwtUtils.getId(refreshToken));

        if (redisTemplate.opsForValue().get(refreshKey) == null) {
            throw new UnauthorizedException("refresh token does not match");
        }

        deleteRefreshToken(refreshToken);

        String accessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        return new TokenDTO(accessToken, newRefreshToken);
    }

    public String generateAccessToken(User user) {
        return jwtUtils.generateToken("access", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.ACCESS_TOKEN_EXPIRED);
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String refreshToken = jwtUtils.generateToken("refresh", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.REFRESH_TOKEN_EXPIRED);

        String refreshKey = "refresh:" + hashToken(refreshToken);

        redisTemplate.opsForValue().set(refreshKey, user.getId().toString(), Duration.ofSeconds(CONSTANT.REFRESH_TOKEN_EXPIRED));
        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("리프레시 토큰이 없습니다.");
        }

        String refreshKey = "refresh:" + hashToken(refreshToken);

        redisTemplate.delete(refreshKey);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
