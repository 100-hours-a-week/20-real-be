package com.real.backend.modules.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InvitedUserCreateRequestDTO {
    private String userEmail;
    private String userName;
    private String apiKey;
}
