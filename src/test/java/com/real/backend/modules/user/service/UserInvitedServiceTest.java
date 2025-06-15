package com.real.backend.modules.user.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.modules.user.domain.InvitedUser;
import com.real.backend.modules.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.modules.user.repository.InvitedUserRepository;

@Transactional
class UserInvitedServiceTest extends UserServiceTest {
    @Autowired
    private UserInvitedService userInvitedService;

    @Autowired
    private InvitedUserRepository invitedUserRepository;

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
}
