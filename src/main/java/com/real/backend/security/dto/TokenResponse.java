package com.real.backend.security.dto;

import lombok.Builder;

public record TokenResponse(String accessToken) {

    @Builder
    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

}
