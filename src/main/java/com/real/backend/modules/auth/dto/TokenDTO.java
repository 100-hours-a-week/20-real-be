package com.real.backend.modules.auth.dto;

public record TokenDTO(
    String accessToken,
    String refreshToken
) {
}
