package com.real.backend.test;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.user.domain.LoginType;
import com.real.backend.domain.user.domain.Role;
import com.real.backend.domain.user.domain.Status;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.response.StatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TokenGenController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // TODO 메인에 업로드시에는 반드시 삭제!!!!
    @PostMapping("/auth/signup")
    public StatusResponse signup(@RequestBody UserRequestDTO userRequestDTO){
        createUser(userRequestDTO);
        return StatusResponse.of(201, "register_success");
    }

    public void createUser(UserRequestDTO data){
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
