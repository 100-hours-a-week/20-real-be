package com.real.backend.modules.user.controller;

import static org.mockito.BDDMockito.*;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.config.SecurityTestConfig;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.dto.ChangeUserInfoRequestDTO;
import com.real.backend.modules.user.dto.ChangeUserRoleRequestDTO;
import com.real.backend.modules.user.dto.LoginResponseDTO;
import com.real.backend.modules.user.service.UserService;
import com.real.backend.security.Session;
import com.real.backend.util.WithMockUser;

@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private Session session;

    @BeforeEach
    void setup() {
        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Test
    @WithMockUser
    @DisplayName("getUserInfo 성공: 유저 정보를 조회한다.")
    void getUserInfo_success() throws Exception {
        LoginResponseDTO dto = new LoginResponseDTO("nickname", Role.STAFF, "profile.jpg");
        given(userService.getUserInfo(anyLong())).willReturn(dto);

        mockMvc.perform(get("/api/v1/users/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("nickname"))
            .andExpect(jsonPath("$.data.role").value(Role.STAFF.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("changeRole 성공: 유저 역할을 변경한다.")
    void changeUserRole_success() throws Exception {
        ChangeUserRoleRequestDTO dto = new ChangeUserRoleRequestDTO("test@email.com", Role.TRAINEE);

        mockMvc.perform(patch("/api/v1/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("OK - 사용자 role이 정상적으로 수정되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("changeRole 실패: 존재하지 않는 Role enum 값을 보내면 400 Bad Request가 반환된다")
    void changeUserRole_BadRequest_invalidEnum() throws Exception {
        String invalidJson = """
    {
        "userEmail": "test@test.com",
        "role": "NOT_A_REAL_ROLE"
    }
    """;

        mockMvc.perform(patch("/api/v1/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<Arguments> emptyChangeUserRole() {
        return Stream.of(
            Arguments.of(null, "TRAINEE"),
            Arguments.of("test@test.com", null)
        );
    }

    @ParameterizedTest
    @WithMockUser
    @MethodSource("emptyChangeUserRole")
    @DisplayName("changeRole 실패: dto의 값이 비어있으면 400 Bad Request가 반환된다")
    void changeUserRole_BadRequest_emptyRole(String userEmail, Role role) throws Exception {
        ChangeUserInfoRequestDTO dto = ChangeUserInfoRequestDTO.builder()
            .userEmail(userEmail)
            .role(role)
            .build();
        String invalidJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(patch("/api/v1/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("changeUserInfo 성공: 사용자의 정보를 구글폼을 토대로 수정한다.")
    void changeUserInfo_success() throws Exception {
        ChangeUserInfoRequestDTO dto = new ChangeUserInfoRequestDTO("arnold.kim(김세호)/풀스택", "test@email.com", Role.TRAINEE, "test-api-key");

        mockMvc.perform(patch("/api/v1/users/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("OK - 사용자가 정상적으로 등록되었습니다."));
    }

    @Test
    @WithMockUser
    @DisplayName("changeUserInfo 실패: 존재하지 않는 Role enum 값을 보내면 400 Bad Request가 반환된다")
    void changeUserInfo_BadRequest_invalidEnum() throws Exception {
        String invalidJson = """
    {
        "userEmail": "test@test.com",
        "userName" : "test",
        "role": "NOT_A_REAL_ROLE",
        "apiKey": "test-api-key"
    }
    """;

        mockMvc.perform(patch("/api/v1/users/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    private static Stream<Arguments> emptyChangeUserInfo() {
        return Stream.of(
            Arguments.of(null, "test", "TRAINEE", "test-api-key"),
            Arguments.of("test@test.com", null, "TRAINEE", "test-api-key"),
            Arguments.of("test@test.com", "test", null, "test-api-key"),
            Arguments.of("test@test.com", "test", "TRAINEE", null)
        );
    }

    @ParameterizedTest
    @WithMockUser
    @MethodSource("emptyChangeUserInfo")
    @DisplayName("changeUserInfo 실패: dto의 값이 비어있으면 400 Bad Request가 반환된다")
    void changeUserInfo_BadRequest_emptyEmail(String userEmail, String userName, Role role, String apiKey) throws Exception {
        ChangeUserInfoRequestDTO result = ChangeUserInfoRequestDTO.builder()
            .userEmail(userEmail)
            .userName(userName)
            .role(role)
            .apiKey(apiKey)
            .build();
        String invalidJson = objectMapper.writeValueAsString(result);

        mockMvc.perform(patch("/api/v1/users/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
}
