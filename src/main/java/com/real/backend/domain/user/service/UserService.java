package com.real.backend.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.dto.LoginResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserFinder userFinder;

    @Transactional(readOnly = true)
    public LoginResponseDTO getUserInfo(Long userId) {
        User user = userFinder.getUser(userId);
        return LoginResponseDTO.from(user);
    }
}
