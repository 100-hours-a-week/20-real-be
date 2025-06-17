package com.real.backend.modules.user.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.config.SecurityTestConfig;
import com.real.backend.modules.user.dto.UserUnreadNoticeResponseDTO;
import com.real.backend.modules.user.service.UserNoticeService;
import com.real.backend.security.Session;
import com.real.backend.util.WithMockUser;

@WebMvcTest(UserNoticeController.class)
@Import(SecurityTestConfig.class)
class UserNoticeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserNoticeService userNoticeService;

    private Session session;

    @BeforeEach
    void setUp() {
        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @DisplayName("getUnreadNotices 성공: 읽지 않은 공지들의 목록을 커서 기반 페이지네이션으로 보여준다.")
    @Test
    @WithMockUser
    void getUnreadNotices_success() throws Exception {
        // given
        List<UserUnreadNoticeResponseDTO> dto = LongStream.range(0, 10)
            .mapToObj(i -> new UserUnreadNoticeResponseDTO(
                i,
                "공지 제목" + i,
                LocalDateTime.now().minusMinutes(i)))
            .toList();

        SliceDTO<UserUnreadNoticeResponseDTO> sliceDTO = SliceDTO.of(dto, null, null, false);
        when(userNoticeService.getNoticeListByCursor(nullable(Long.class), anyInt(), nullable(String.class), eq(1L))).thenReturn(sliceDTO);

        // when & then
        mockMvc.perform(get("/api/v1/users/notices/unread")
                .queryParam("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items", hasSize(10)))
            .andExpect(jsonPath("$.data.nextCursorStandard").value(nullValue()))
            .andExpect(jsonPath("$.data.nextCursorId").value(nullValue()))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @DisplayName("getUnreadNotices 성공: limit 파라미터 없이 요청하면 기본값 5가 사용됨")
    @Test
    @WithMockUser
    void getUnreadNotices_defaultLimit() throws Exception {
        // given
        List<UserUnreadNoticeResponseDTO> dto = LongStream.range(0, 10)
            .mapToObj(i -> new UserUnreadNoticeResponseDTO(
                i,
                "공지 제목" + i,
                LocalDateTime.now().minusMinutes(i)))
            .toList();

        // when
        mockMvc.perform(get("/api/v1/users/notices/unread"))
            .andExpect(status().isOk());

        // then
        verify(userNoticeService).getNoticeListByCursor(nullable(Long.class), eq(5), nullable(String.class), eq(1L));
    }

    @DisplayName("getUnreadNotice 실패: limit가 음수가 들어오면 실패")
    @Test
    @WithMockUser
    void getUnreadNotices_negativeLimit() throws Exception {
        // given
        List<UserUnreadNoticeResponseDTO> dto = LongStream.range(0, 10)
            .mapToObj(i -> new UserUnreadNoticeResponseDTO(
                i,
                "공지 제목" + i,
                LocalDateTime.now().minusMinutes(i)))
            .toList();

        // when & then
        mockMvc.perform(get("/api/v1/users/notices/unread")
                .queryParam("limit", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
}
