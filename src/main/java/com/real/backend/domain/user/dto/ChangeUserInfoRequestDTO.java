package com.real.backend.domain.user.dto;

import com.real.backend.domain.user.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserInfoRequestDTO {
    private String userName;
    private String userEmail;
    private Role role;
    private String apiKey;
}
