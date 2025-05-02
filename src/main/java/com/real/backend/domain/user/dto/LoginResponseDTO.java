package com.real.backend.domain.user.dto;

import com.real.backend.domain.user.domain.Role;
import com.real.backend.domain.user.domain.User;

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

    public static LoginResponseDTO from(User user) {
        return LoginResponseDTO.builder()
            .nickname(user.getNickname())
            .role(user.getRole())
            .profileUrl(user.getProfileUrl())
            .build();
    }
}
