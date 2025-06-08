package com.real.backend.modules.auth.service;

import org.springframework.stereotype.Service;

import com.real.backend.common.util.CONSTANT;
import com.real.backend.modules.user.domain.User;
import com.real.backend.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccessTokenService {
    private final JwtUtils jwtUtils;

    public String generateAccessToken(User user) {
        return jwtUtils.generateToken("access", user.getId(), user.getNickname(), user.getRole().toString(),
            CONSTANT.ACCESS_TOKEN_EXPIRED);
    }
}
