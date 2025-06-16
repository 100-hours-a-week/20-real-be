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
import com.real.backend.modules.user.dto.LoginResponseDTO;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
class UserGetInfoTest extends UserServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @DisplayName("사용자 정보를 가져오는데 성공")
    @Test
    void getUserInfoSuccess() {
        // given
        User user = User.builder()
            .email("email@email.com")
            .nickname("nickname")
            .loginType(LoginType.OAUTH)
            .role(Role.TRAINEE)
            .status(Status.NORMAL)
            .profileUrl("www.kakaotech.com/")
            .lastLoginAt(LocalDateTime.now())
            .signupAt(LocalDateTime.now())
            .build();

        userRepository.saveAndFlush(user);
        Long userId = user.getId();

        // when
        LoginResponseDTO result = userService.getUserInfo(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(Role.TRAINEE);
        assertThat(result.getNickname()).isEqualTo("nickname");
        assertThat(result.getProfileUrl()).isEqualTo("www.kakaotech.com/");

    }

    @DisplayName("존재하지 않는 사용자 ID로 정보 조회 시 실패")
    @Test
    void getUserInfoFail() {
        // given
        Long invalidUserId = 9999L;

        // when & then
        assertThatThrownBy(() -> userService.getUserInfo(invalidUserId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 id를 가진 사용자가 없습니다.");
    }
}
