package com.real.backend.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.dto.ChangeUserRoleRequestDTO;
import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.exception.ForbiddenException;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    @Value("${spring.api.secret}")
    private String apiKey;

    private final UserFinder userFinder;

    @Transactional(readOnly = true)
    public LoginResponseDTO getUserInfo(Long userId) {
        User user = userFinder.getUser(userId);
        return LoginResponseDTO.from(user);
    }

    @Transactional
    public void changeUserRole(ChangeUserRoleRequestDTO changeUserRoleRequestDTO) {
        User user = userRepository.findByEmail(changeUserRoleRequestDTO.getUserEmail()).orElseThrow(() -> new NotFoundException("해당 이메일을 가진 사용자가 없습니다."));

        if (!apiKey.equals(changeUserRoleRequestDTO.getApiKey())) {
            throw new ForbiddenException("권한이 없습니다.");
        }

        user.updateRole(changeUserRoleRequestDTO.getRole());
        userRepository.save(user);
    }
}
