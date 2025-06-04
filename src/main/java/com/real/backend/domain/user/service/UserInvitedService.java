package com.real.backend.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.user.domain.InvitedUser;
import com.real.backend.domain.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.domain.user.repository.InvitedUserRepository;
import com.real.backend.exception.ForbiddenException;

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
}
