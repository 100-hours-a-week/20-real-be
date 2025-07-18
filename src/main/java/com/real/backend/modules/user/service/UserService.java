package com.real.backend.modules.user.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.dto.ChangeUserInfoRequestDTO;
import com.real.backend.modules.user.dto.ChangeUserRoleRequestDTO;
import com.real.backend.modules.user.dto.LoginResponseDTO;
import com.real.backend.modules.user.repository.UserRepository;
import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.common.exception.NotFoundException;

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

        user.updateRole(changeUserRoleRequestDTO.getRole());
        userRepository.save(user);
    }

    @Transactional
    public void enrollUser(ChangeUserInfoRequestDTO changeUserInfoRequestDTO) {
        User user = userRepository.findByEmail(changeUserInfoRequestDTO.getUserEmail()).orElseThrow(() -> new NotFoundException("해당 이메일을 가진 사용자가 없습니다."));

        if (!apiKey.equals(changeUserInfoRequestDTO.getApiKey())) {
            throw new ForbiddenException("권한이 없습니다.");
        }

        user.enroll(changeUserInfoRequestDTO.getUserName(), changeUserInfoRequestDTO.getRole());
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLoginTime(User user) {
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
