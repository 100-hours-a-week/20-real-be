package com.real.backend.test;

import com.real.backend.domain.user.domain.LoginType;
import com.real.backend.domain.user.domain.Role;
import com.real.backend.domain.user.domain.Status;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String nickname;
    private String password;
    private Role role;
    private String email;
    private LoginType loginType;
    private Status status;
}
