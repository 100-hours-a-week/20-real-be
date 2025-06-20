package com.real.backend.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvitedUserCreateRequestDTO {
    @NotBlank
    private String userEmail;

    @NotBlank
    private String userName;

    @NotBlank
    private String apiKey;
}
