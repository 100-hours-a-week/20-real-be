package com.real.backend.modules.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.modules.user.domain.InvitedUser;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.modules.user.repository.InvitedUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInvitedService {
    @Value("${spring.api.secret}")
    private String apiKey;

    private final InvitedUserRepository invitedUserRepository;

    @Transactional
    public void createInvitedUser(InvitedUserCreateRequestDTO invitedUserCreateRequestDTO) {
        if (!apiKey.equals(invitedUserCreateRequestDTO.getApiKey())) {
            throw new ForbiddenException("권한이 없습니다.");
        }
        invitedUserRepository.save(
            InvitedUser.builder()
                .email(invitedUserCreateRequestDTO.getUserEmail())
                .name(invitedUserCreateRequestDTO.getUserName())
                .build()
        );
    }

    @Transactional
    public User setUserInfoWithInvitedUser(User user) {
        InvitedUser invitedUser = invitedUserRepository.findByEmail(user.getEmail()).orElse(null);

        if (invitedUser != null) {
            user.enroll(invitedUser.getName(), Role.TRAINEE);
        }
        return user;
    }
}
