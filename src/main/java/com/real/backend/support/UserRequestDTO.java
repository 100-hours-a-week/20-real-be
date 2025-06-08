package com.real.backend.support;

import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String nickname;
    private String password;
    private Role role;
    private String email;
    private LoginType loginType;
    private Status status;
    private String apiKey;
}
