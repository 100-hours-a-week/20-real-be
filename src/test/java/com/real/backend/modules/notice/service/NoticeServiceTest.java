package com.real.backend.modules.notice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.common.config.RedisConfig;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.config.AiResponseConfig;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.modules.notice.dto.NoticeFileGroups;
import com.real.backend.modules.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.modules.notice.dto.NoticeListResponseDTO;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
@Rollback
@Import({RedisConfig.class, AiResponseConfig.class})
class NoticeServiceTest extends NoticeServiceTestIntegration {
    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoticeRedisService noticeRedisService;

    @Autowired
    private PostRedisService postRedisService;

    @MockitoBean
    private NoticeAiService noticeAiService;

    @MockitoBean
    private NoticeFileService noticeFileService;

    @DisplayName("getNoticeListByCursor 성공: 읽음 여부 확인 및 공지의 첫 페이지를 최대 10개까지 보여준다.")
    @Test
    void getNoticeListByCursor_success() {
        // given
        User user = createUser();
        Long userId = user.getId();
        List<Notice> notices = createNotices(user, 5);

        List<Long> readIds = List.of(notices.get(0).getId(), notices.get(1).getId());
        readIds.forEach(id -> {
            noticeRedisService.createUserNoticeRead(userId, id);
        });

        // when
        SliceDTO<NoticeListResponseDTO> result = noticeService.getNoticeListByCursor(null, 10, null, userId);

        // then
        assertThat(result.items()).hasSize(5);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.items().get(4).getUserRead()).isTrue();
        assertThat(result.items().get(2).getUserRead()).isFalse();
    }

    @DisplayName("getNoticeListByCursor 성공: 안읽은 공지가 (notice의 개수 - limit)개 만큼 더 남아 있음")
    @Test
    void getNoticeListByCursor_success_hasNextTrue() {
        // given
        int limit = 10;
        User user = createUser();
        Long userId = user.getId();
        List<Notice> notices = createNotices(user, 20);

        // when
        SliceDTO<NoticeListResponseDTO> result = noticeService.getNoticeListByCursor(null, limit, null, userId);

        // then
        assertThat(result.items()).hasSize(limit);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursorId()).isEqualTo(notices.get(10).getId());
        assertThat(result.nextCursorStandard()).isEqualTo(notices.get(10).getCreatedAt().toString());
    }

    @DisplayName("getNoticeListByCursor 성공: 중간부터 공지를 읽고, 뒤에 더이상 남은 공지가 없음")
    @Test
    void getNoticeListByCursor_success_hasNextFalse() {
        // given
        int limit = 10;
        User user = createUser();
        Long userId = user.getId();
        List<Notice> notices = createNotices(user, 20);

        // when
        SliceDTO<NoticeListResponseDTO> result = noticeService.getNoticeListByCursor(notices.get(10).getId(), limit, notices.get(10).getCreatedAt().toString(), userId);

        // then
        assertThat(result.items()).hasSize(10);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursorId()).isNull();
        assertThat(result.nextCursorStandard()).isNull();
        List<Long> resultIds = result.items().stream()
            .map(NoticeListResponseDTO::getId)
            .toList();
        List<Long> previousIds = IntStream.range(10, 20)
            .mapToObj(i -> notices.get(i).getId())
            .toList();
        assertThat(resultIds).doesNotContainAnyElementsOf(previousIds);
    }

    @DisplayName("createNotice 성공: 공지가 정상적으로 생성")
    @Test
    void createNotice_success() throws JsonProcessingException {
        // given
        User user = createUser();
        Long userId = user.getId();
        LocalDateTime now = LocalDateTime.now();
        NoticeCreateRequestDTO dto = NoticeCreateRequestDTO.builder()
            .title("제목")
            .content("내용")
            .tag("공지")
            .userName("테스터")
            .originalUrl("https://example.com/original")
            .platform("디스코드")
            .createdAt(now.toString())
            .build();

        List<MultipartFile> images = List.of(new MockMultipartFile("image1", "image1.png", "image/png", "fake image content".getBytes()));
        List<MultipartFile> files = List.of(new MockMultipartFile("file1", "file1.pdf", "application/pdf", "fake file content".getBytes()));

        NoticeSummaryResponseDTO mockSummary = new NoticeSummaryResponseDTO("요약된 내용", true);
        given(noticeAiService.makeSummary(any(NoticeSummaryRequestDTO.class))).willReturn(mockSummary);


        // when
        noticeService.createNotice(dto, images, files);

        // then
        List<Notice> result = noticeRepository.findAll();
        assertThat(result).hasSize(1);

        Notice notice = result.get(0);
        assertThat(notice.getTitle()).isEqualTo("제목");
        assertThat(notice.getContent()).isEqualTo("내용");
        assertThat(notice.getUser().getId()).isEqualTo(userId);
        assertThat(notice.getTag()).isEqualTo("공지");
        assertThat(notice.getOriginalUrl()).isEqualTo("https://example.com/original");
        assertThat(notice.getSummary()).isEqualTo("요약된 내용");
        assertThat(notice.getDeletedAt()).isNull();
        assertThat(notice.getTotalViewCount()).isEqualTo(0);
        assertThat(notice.getCommentCount()).isEqualTo(0);
        assertThat(notice.getLikeCount()).isEqualTo(0);
        assertThat(notice.getCreatedAt()).isEqualTo(now);

        verify(noticeFileService, times(1)).uploadFilesToS3(images, notice, true);
        verify(noticeFileService, times(1)).uploadFilesToS3(files, notice, false);
    }

    @DisplayName("createNotice 실패: 유효하지 않은 작성자 이름으로 인해 공지 작성 실패")
    @Test
    void createNotice_fail_invalidUsername() throws JsonProcessingException {
        // given
        LocalDateTime createdAt = LocalDateTime.now();
        NoticeCreateRequestDTO dto = NoticeCreateRequestDTO.builder()
            .title("제목")
            .content("내용")
            .tag("공지")
            .userName("없는 유저")
            .originalUrl("https://example.com/original")
            .platform("디스코드")
            .createdAt(createdAt.toString())
            .build();

        List<MultipartFile> images = List.of(new MockMultipartFile("image1", "image1.png", "image/png", "fake image content".getBytes()));
        List<MultipartFile> files = List.of(new MockMultipartFile("file1", "file1.pdf", "application/pdf", "fake file content".getBytes()));

        NoticeSummaryResponseDTO mockSummary = new NoticeSummaryResponseDTO("요약된 내용", true);
        given(noticeAiService.makeSummary(any(NoticeSummaryRequestDTO.class)))
            .willReturn(mockSummary);

        // when & then
        assertThatThrownBy(() -> noticeService.createNotice(dto, images, files))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 이름을 가진 사용자가 없습니다.");
    }



    @DisplayName("getNoticeById 성공: 공지 하나의 정보를 성공적으로 불러옴")
    @Test
    void getNoticeById_success() {
        // given
        User user = createUser();
        Long userId = user.getId();
        Notice notice = createNotices(user, 3).getFirst();
        Long noticeId = notice.getId();

        postRedisService.initCount("notice", "totalView", noticeId, 0L);
        postRedisService.initCount("notice", "like", noticeId, 2L);
        postRedisService.initCount("notice", "comment", noticeId, 5L);
        postRedisService.toggleLikeInRedis("notice", userId, noticeId);

        given(noticeFileService.getNoticeFileGroups(notice))
            .willReturn(new NoticeFileGroups(List.of(), List.of()));

        // when
        NoticeInfoResponseDTO result = noticeService.getNoticeById(noticeId, userId);

        // then
        assertThat(result.getId()).isEqualTo(noticeId);
        assertThat(result.getTitle()).isEqualTo(notice.getTitle());
        assertThat(result.getContent()).isEqualTo(notice.getContent());
        assertThat(result.getLikeCount()).isEqualTo(3L);
        assertThat(result.getCommentCount()).isEqualTo(5L);
        assertThat(result.getUserLike()).isTrue();
        assertThat(result.getFiles()).isEmpty();
        assertThat(result.getImages()).isEmpty();
    }

    @DisplayName("getNoticeById 실패: 삭제된 공지를 불러오려고 해서 실패")
    @Test
    void getNoticeById_fail_deletedNotice() {
        // given
        User user = createUser();
        Long userId = user.getId();
        Notice notice = createNotices(null, 3).getFirst();
        Long noticeId = notice.getId();
        notice.delete();

        // when & then
        assertThatThrownBy(() -> noticeService.getNoticeById(noticeId, userId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 id를 가진 공지가 존재하지 않습니다.");
    }

    @DisplayName("getNoticeById 실패: 공지가 존재하지 않음 ")
    @Test
    void getNoticeById_fail_noticeNotFound() {
        // given
        User user = createUser();
        Long userId = user.getId();
        Long noticeId = 1L;

        // when & then
        assertThatThrownBy(() -> noticeService.getNoticeById(noticeId, userId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 id를 가진 공지가 존재하지 않습니다.");
    }

    @DisplayName("deleteNotice 성공: 공지 삭제 요청이 오면 해당 공지의 deletedAt을 now()로 변경한다.")
    @Test
    void deleteNotice_success() {
        // given
        Notice notice = createNotices(createUser(), 1).getFirst();
        Long noticeId = notice.getId();

        // when
        noticeService.deleteNotice(noticeId);

        // then
        Notice result = noticeRepository.findById(noticeId).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getDeletedAt()).isNotNull();
    }

    @DisplayName("deleteNotice 실패: 공지가 존재하지 않음.")
    @Test
    void deleteNotice_fail_noticeNotFound() {
        // given
        Long noticeId = 1L;

        // when & then
        assertThatThrownBy(() -> noticeService.deleteNotice(noticeId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("해당 id를 가진 공지가 존재하지 않습니다.");
    }


    private User createUser() {
        User user = User.builder()
            .email("notice@test.com")
            .nickname("테스터")
            .role(Role.STAFF)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();
        return userRepository.save(user);
    }

    private List<Notice> createNotices(User user, int size) {
        List<Notice> notices = (List<Notice>)IntStream.range(0, size)
            .mapToObj(i -> Notice.builder()
                .title("공지 " + i)
                .content("내용 " + i)
                .commentCount(0L) // null 방지
                .likeCount(0L)
                .totalViewCount(0L)
                .tag("뉴스")
                .originalUrl("url.com")
                .platform("discord")
                .summary("요약" + i)
                .user(user)
                .build())
            .toList();
        return noticeRepository.saveAllAndFlush(notices);
    }
}
