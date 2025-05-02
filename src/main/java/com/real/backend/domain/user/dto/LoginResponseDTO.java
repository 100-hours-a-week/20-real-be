package com.real.backend.domain.user.dto;

import com.real.backend.domain.user.domain.Role;

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
}
