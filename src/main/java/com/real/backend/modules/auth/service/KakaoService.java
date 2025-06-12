package com.real.backend.modules.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.util.S3Utils;
import com.real.backend.modules.auth.dto.KakaoProfileDTO;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;
import com.real.backend.modules.user.service.UserInvitedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final UserInvitedService userInvitedService;
    private final S3Utils s3Utils;

    @Transactional
    public User createKakaoUser(KakaoProfileDTO kakaoProfile) {
        String profileUrl = s3Utils.getRandomDefaultProfileUrl();

        User user = User.builder()
            .email(kakaoProfile.getKakao_account().getEmail())
            .nickname(kakaoProfile.getProperties().getNickname())
            .profileUrl(profileUrl)
            .loginType(LoginType.OAUTH)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build();

        user = userInvitedService.setUserInfoWithInvitedUser(user);

        userRepository.save(user);
        return user;
    }

}
