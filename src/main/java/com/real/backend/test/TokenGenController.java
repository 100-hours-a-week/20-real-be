package com.real.backend.test;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.user.domain.LoginType;
import com.real.backend.domain.user.domain.Role;
import com.real.backend.domain.user.domain.Status;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.exception.ForbiddenException;
import com.real.backend.response.StatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TokenGenController {
    @Value("${spring.api.secret}")
    private String apiKey;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/auth/signup")
    public StatusResponse signup(@RequestBody UserRequestDTO userRequestDTO){
        createUser(userRequestDTO);
        return StatusResponse.of(201, "register_success");
    }

    public void createUser(UserRequestDTO data){
        if (!data.getApiKey().equals(apiKey)) {
            throw new ForbiddenException("접근할 수 없는 api입니다.");
        }
        userRepository.save(
            User.builder()
                .email(data.getEmail())
                .nickname(data.getNickname())
                .password(bCryptPasswordEncoder.encode(data.getPassword()))
                .loginType(LoginType.NORMAL)
                .role(Role.STAFF)
                .status(Status.NORMAL)
                .lastLoginAt(LocalDateTime.now())
                .build());
    }
}
