package com.real.backend.modules.auth.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.UnauthorizedException;
import com.real.backend.modules.auth.dto.TokenDTO;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;
    private final JwtUtils jwtUtils;
    private final UserFinder userFinder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public TokenDTO refreshAccessToken(String refreshToken) {
        String refreshKey = "refresh:" + refreshToken;
        if (refreshToken == null) { throw new UnauthorizedException("refresh token is null"); }

        User user = userFinder.getUser(jwtUtils.getId(refreshToken));

        if (redisTemplate.opsForValue().get(refreshKey) == null) {
            throw new UnauthorizedException("refresh token does not match");
        }

        refreshTokenService.deleteRefreshToken(refreshToken);

        String accessToken = accessTokenService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.generateRefreshToken(user);

        return new TokenDTO(accessToken, newRefreshToken);
    }
}
