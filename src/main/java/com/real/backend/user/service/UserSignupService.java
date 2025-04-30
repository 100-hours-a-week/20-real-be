package com.real.backend.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.oauth.dto.KakaoProfileDTO;
import com.real.backend.user.domain.InvitedUser;
import com.real.backend.user.domain.LoginType;
import com.real.backend.user.domain.Role;
import com.real.backend.user.domain.Status;
import com.real.backend.user.domain.User;
import com.real.backend.user.repository.InvitedUserRepository;
import com.real.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSignupService {

    private final UserRepository userRepository;
    private final InvitedUserRepository invitedUserRepository;

    @Transactional
    public User setKtbUser(String email, User user) {
        InvitedUser invitedUser = invitedUserRepository.findByEmail(email).orElse(null);
        if (invitedUser != null) {
            user.updateNickname(invitedUser.getName());
            user.updateRole(Role.TRAINEE);
        }
        return user;
    }

    // TODO 프로필 사진 s3 버킷 연결
    // TODO Role 설정
    @Transactional
    public User createOAuthUser(KakaoProfileDTO kakaoProfile) {
        User user = setKtbUser(kakaoProfile.getKakao_account().getEmail(), User.builder()
            .email(kakaoProfile.getKakao_account().getEmail())
            .nickname(kakaoProfile.getProperties().getNickname())
            // .profileUrl("")
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
        User user = User.builder()
            .email(email)
            .nickname(nickname)
            // .profileUrl("")
            .loginType(LoginType.NORMAL)
            .role(Role.OUTSIDER)
            .status(Status.NORMAL)
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.save(user);
        return user;
    }

}
