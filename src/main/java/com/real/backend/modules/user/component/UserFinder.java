package com.real.backend.modules.user.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;
import com.real.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("해당 id를 가진 사용자가 없습니다."));
        if (user.getWithdrawAt() != null) {
            throw new NotFoundException("해당 id를 가진 사용자가 없습니다.");
        }
        return user;
    }
}
