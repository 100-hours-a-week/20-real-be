package com.real.backend.modules.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.NotFoundException;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.dto.ChangeUserRoleRequestDTO;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
class UserChangeInfoTest extends UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @DisplayName("changeUserRole 성공: 사용자 정보 변경 성공")
    @Test
    void test() {
        // given
        User user = User.builder()
            .email("user@email.com")
            .nickname("user")
            .role(Role.OUTSIDER)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();

        userRepository.saveAndFlush(user);

        ChangeUserRoleRequestDTO request = ChangeUserRoleRequestDTO.builder()
            .userEmail("user@email.com")
            .role(Role.TRAINEE)
            .build();

        // when
        userService.changeUserRole(request);

        // then
        User updatedUser = userRepository.findByEmail("user@email.com").get();
        assertThat(updatedUser.getRole()).isEqualTo(Role.TRAINEE);
    }

    @Test
    @DisplayName("changeUserRole 실패: 없는 이메일로 역할 변경 시 예외가 발생한다")
    void changeUserRoleFail_EmailNotFound() {
        // given
        ChangeUserRoleRequestDTO dto = ChangeUserRoleRequestDTO.builder()
            .userEmail("nonexistent@email.com")
            .role(Role.TRAINEE)
            .build();

        // when & then
        assertThatThrownBy(() -> userService.changeUserRole(dto))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 이메일을 가진 사용자가 없습니다.");
    }
}
