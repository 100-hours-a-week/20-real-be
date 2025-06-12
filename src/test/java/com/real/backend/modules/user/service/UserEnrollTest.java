package com.real.backend.modules.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.dto.ChangeUserInfoRequestDTO;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
class UserEnrollTest extends UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${spring.api.secret}")
    private String apiKey;

    @Test
    @DisplayName("enrollUser 성공: 이름과 역할 변경")
    void enrollUserSuccess() {
        // given
        User user = User.builder()
            .email("test@email.com")
            .nickname("김세호")
            .role(Role.OUTSIDER)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.saveAndFlush(user);

        ChangeUserInfoRequestDTO dto = ChangeUserInfoRequestDTO.builder()
            .userEmail("test@email.com")
            .userName("arnold.kim(김세호)/풀스택")
            .role(Role.TRAINEE)
            .apiKey(apiKey)
            .build();

        // when
        userService.enrollUser(dto);

        // then
        User updated = userRepository.findByEmail("test@email.com").get();
        assertThat(updated.getNickname()).isEqualTo("arnold.kim(김세호)/풀스택");
        assertThat(updated.getRole()).isEqualTo(Role.TRAINEE);
    }

    @Test
    @DisplayName("enrollUser 실패: 존재하지 않는 이메일")
    void enrollUserFail_notFound() {
        // given
        ChangeUserInfoRequestDTO dto = ChangeUserInfoRequestDTO.builder()
            .userEmail("test@email.com")
            .userName("arnold.kim(김세호)/풀스택")
            .role(Role.OUTSIDER)
            .apiKey(apiKey)
            .build();

        // when & then
        assertThatThrownBy(() -> userService.enrollUser(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 이메일을 가진 사용자가 없습니다.");
    }

    @Test
    @DisplayName("enrollUser 실패: API 키 불일치")
    void enrollUserFail_forbidden() {
        // given
        User user = User.builder()
            .email("test@email.com")
            .nickname("김세호")
            .role(Role.OUTSIDER)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.saveAndFlush(user);

        ChangeUserInfoRequestDTO dto = ChangeUserInfoRequestDTO.builder()
            .userEmail("test@email.com")
            .userName("arnold.kim(김세호)/풀스택")
            .role(Role.TRAINEE)
            .apiKey("invalid-key")
            .build();

        // when & then
        assertThatThrownBy(() -> userService.enrollUser(dto))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("권한이 없습니다.");
    }
}
