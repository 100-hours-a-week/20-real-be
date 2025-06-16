package com.real.backend.modules.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.config.SecurityTestConfig;
import com.real.backend.modules.user.dto.InvitedUserCreateRequestDTO;
import com.real.backend.security.Session;
import com.real.backend.util.WithMockUser;

@WebMvcTest(UserInvitedController.class)
@Import(SecurityTestConfig.class)
class UserInvitedControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Session session;

    @BeforeEach
    void setup() {
        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @DisplayName("getUserInfo 성공: 유저 정보를 조회한다.")
    @Test
    @WithMockUser
    void createInvitedUser_success() throws Exception {
        // given
        InvitedUserCreateRequestDTO dto = InvitedUserCreateRequestDTO.builder()
            .userEmail("test@email.com")
            .userName("test")
            .apiKey("test-api-key")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/invited-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Created - 초대 유저가 정상적으로 생성되었습니다."));
    }

    private static Stream<Arguments> emptyInvitedUserCreate() {
        return Stream.of(
            Arguments.of(null, "test", "test-api-key"),
            Arguments.of("test@test.com", null, "test-api-key"),
            Arguments.of("test@test.com", "test", null)
        );
    }

    @DisplayName("getUserInfo 실패: dto의 값이 비어있으면 400 Bad Request가 반환된다")
    @ParameterizedTest
    @WithMockUser
    @MethodSource("emptyInvitedUserCreate")
    void createInvitedUser_emptyValue(String userEmail, String userName, String apiKey) throws Exception {
        // given
        InvitedUserCreateRequestDTO dto = InvitedUserCreateRequestDTO.builder()
            .userEmail(userEmail)
            .userName(userName)
            .apiKey(apiKey)
            .build();

        String invalidJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/invited-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());

    }

}
