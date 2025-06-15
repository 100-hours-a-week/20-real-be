package com.real.backend.modules.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.modules.user.domain.InvitedUser;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.modules.user.repository.InvitedUserRepository;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
class UserInvitedServiceTest extends UserServiceTest {
    @Autowired
    private UserInvitedService userInvitedService;

    @Autowired
    private InvitedUserRepository invitedUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.api.secret}")
    private String apiKey;

    @Test
    @DisplayName("createInvitedUser 성공: 올바른 API 키로 초대 유저 생성에 성공한다")
    void createInvitedUserSuccess() {
        // given
        InvitedUserCreateRequestDTO dto = InvitedUserCreateRequestDTO.builder()
            .userEmail("invite@test.com")
            .userName("초대자")
            .apiKey(apiKey)
            .build();

        // when
        userInvitedService.createInvitedUser(dto);

        // then
        InvitedUser saved = invitedUserRepository.findByEmail("invite@test.com")
            .orElseThrow(() -> new AssertionError("유저가 저장되지 않았습니다."));
        assertThat(saved.getName()).isEqualTo("초대자");
    }

    @Test
    @DisplayName("createInvitedUser 실패: 잘못된 API 키로 초대 유저 생성 시 실패한다")
    void createInvitedUserFail_invalidApiKey() {
        // given
        InvitedUserCreateRequestDTO dto = InvitedUserCreateRequestDTO.builder()
            .userEmail("unauthorized@test.com")
            .userName("잘못된 요청")
            .apiKey("wrong-api-key")
            .build();

        // when & then
        assertThatThrownBy(() -> userInvitedService.createInvitedUser(dto))
            .isInstanceOf(ForbiddenException.class)
            .hasMessageContaining("권한이 없습니다.");
    }

    @Test
    @DisplayName("setUserInfoWithInvitedUser 성공: 초대 유저 정보가 있을 경우 사용자 정보가 설정된다")
    void setUserInfoWithInvitedUser_success() {
        // given
        String email = "invited@user.com";

        InvitedUser invited = InvitedUser.builder()
            .email(email)
            .name("초대자")
            .build();
        invitedUserRepository.saveAndFlush(invited);

        User user = User.builder()
            .email(email)
            .nickname("기본닉네임")
            .role(Role.OUTSIDER)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();
        userRepository.saveAndFlush(user);

        // when
        User updated = userInvitedService.setUserInfoWithInvitedUser(user);

        // then
        assertThat(updated.getNickname()).isEqualTo("초대자");
        assertThat(updated.getRole()).isEqualTo(Role.TRAINEE);
    }

    @Test
    @DisplayName("setUserInfoWithInvitedUser 성공: 초대 유저 정보가 없을 경우 사용자 정보는 변경되지 않는다")
    void setUserInfoWithInvitedUser_noMatch() {
        // given
        String email = "not-invited@user.com";

        User user = User.builder()
            .email(email)
            .nickname("원래닉네임")
            .role(Role.OUTSIDER)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();
        userRepository.saveAndFlush(user);

        // when
        User result = userInvitedService.setUserInfoWithInvitedUser(user);

        // then
        assertThat(result.getNickname()).isEqualTo("원래닉네임");
        assertThat(result.getRole()).isEqualTo(Role.OUTSIDER);
    }
}
