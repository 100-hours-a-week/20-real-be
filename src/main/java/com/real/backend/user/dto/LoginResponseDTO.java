package com.real.backend.user.dto;

import com.real.backend.user.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String nickname;
    private Role role;
    private String profileUrl;
    private String accessToken;
}
