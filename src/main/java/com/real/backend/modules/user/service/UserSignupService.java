package com.real.backend.modules.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.util.S3Utils;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final UserRepository userRepository;
    private final S3Utils s3Utils;

    @Transactional
    public User createNormalUser(String email, String nickname) {
        String profileUrl = s3Utils.getRandomDefaultProfileUrl();

        User user = User.builder()
            .email(email)
            .nickname(nickname)
            .profileUrl(profileUrl)
            .loginType(LoginType.NORMAL)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.save(user);
        return user;
    }

}
