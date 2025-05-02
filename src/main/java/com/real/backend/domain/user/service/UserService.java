package com.real.backend.domain.user.service;

import org.springframework.stereotype.Service;

import com.real.backend.exception.NotFoundException;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("해당 id를 가진 사용자가 없습니다."));
    }
}
