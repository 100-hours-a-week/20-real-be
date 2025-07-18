package com.real.backend.modules.user.dto;

import com.real.backend.modules.user.domain.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserInfoRequestDTO {
    @NotBlank
    private String userName;
    @NotBlank
    private String userEmail;
    @NotNull
    private Role role;
    @NotBlank
    private String apiKey;
}
