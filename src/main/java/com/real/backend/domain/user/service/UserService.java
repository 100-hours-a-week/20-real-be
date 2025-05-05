package com.real.backend.domain.user.service;

import org.springframework.stereotype.Service;

import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.exception.NotFoundException;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserFinder userFinder;

    public LoginResponseDTO getUserInfo(Long userId) {
        User user = userFinder.getUser(userId);
        return LoginResponseDTO.from(user);
    }
}
