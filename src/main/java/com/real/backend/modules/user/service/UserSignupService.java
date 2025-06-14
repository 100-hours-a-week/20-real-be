package com.real.backend.modules.user.service;

import com.real.backend.common.util.S3Utils;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.auth.dto.KakaoProfileDTO;
import com.real.backend.modules.user.domain.InvitedUser;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.InvitedUserRepository;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final UserRepository userRepository;
    private final InvitedUserRepository invitedUserRepository;
    private final S3Utils s3Utils;

    @Transactional
    public User setKtbUser(String email, User user) {
        InvitedUser invitedUser = invitedUserRepository.findByEmail(email).orElse(null);
        if (invitedUser != null) {
            user.updateNickname(invitedUser.getName());
            user.updateRole(Role.TRAINEE);
        }

        return user;
    }

    @Transactional
    public User createOAuthUser(KakaoProfileDTO kakaoProfile) {
        String imageUrl = s3Utils.getRandomDefaultProfileUrl();

        User user = setKtbUser(kakaoProfile.getKakao_account().getEmail(), User.builder()
            .email(kakaoProfile.getKakao_account().getEmail())
            .nickname(kakaoProfile.getProperties().getNickname())
            .profileUrl(imageUrl)
            .loginType(LoginType.OAUTH)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build());

        userRepository.save(user);
        return user;
    }

    @Transactional
    public User createNormalUser(String email, String nickname) {
        String imageUrl = s3Utils.getRandomDefaultProfileUrl();

        User user = User.builder()
            .email(email)
            .nickname(nickname)
            // .profileUrl(imageUrl)
            .loginType(LoginType.NORMAL)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.save(user);
        return user;
    }

}
