package com.real.backend.modules.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.UnauthorizedException;
import com.real.backend.common.util.CONSTANT;
import com.real.backend.modules.user.domain.User;
import com.real.backend.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtils jwtUtils;

    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String generateRefreshToken(User user) {
        String refreshToken = jwtUtils.generateToken("refresh", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.REFRESH_TOKEN_EXPIRED);

        String refreshKey = "refresh:" + refreshToken;

        redisTemplate.opsForValue().set(refreshKey, user.getId().toString(), Duration.ofSeconds(CONSTANT.REFRESH_TOKEN_EXPIRED));
        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("리프레시 토큰이 없습니다.");
        }
        redisTemplate.delete("refresh:" + refreshToken);
    }
}
