package com.real.backend.domain.user.component;

import org.springframework.stereotype.Component;

import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("해당 id를 가진 사용자가 없습니다."));
        if (user.getWithdrawAt() != null) {
            throw new NotFoundException("해당 id를 가진 사용자가 없습니다.");
        }
        return user;
    }
}
