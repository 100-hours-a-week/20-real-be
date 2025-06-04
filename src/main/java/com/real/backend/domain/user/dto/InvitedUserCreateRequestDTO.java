package com.real.backend.domain.user.dto;

import lombok.Getter;

@Getter
public class InvitedUserCreateRequestDTO {
    private String userEmail;
    private String userName;
    private String apiKey;
}
