package com.real.backend.modules.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.config.RedisConfig;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.LoginType;
import com.real.backend.modules.user.domain.Role;
import com.real.backend.modules.user.domain.Status;
import com.real.backend.modules.user.domain.User;
import com.real.backend.modules.user.domain.UserNoticeRead;
import com.real.backend.modules.user.dto.UserUnreadNoticeResponseDTO;
import com.real.backend.modules.user.repository.UserNoticeReadRepository;
import com.real.backend.modules.user.repository.UserRepository;

@Transactional
@Import(RedisConfig.class)
class UserNoticeServiceTest extends UserServiceTest {
    @Autowired
    private UserNoticeService userNoticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeRedisService noticeRedisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserNoticeReadRepository userNoticeReadRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserFinder userFinder;

    @AfterEach
    void tearDown() {
        String key = "notice:read:user:1";
        redisTemplate.delete(key);
    }

    @DisplayName("getNoticeListByCursor 성공: 레디스에 읽은 공지가 2개 있고, 공지는 5개 있을 때 안 읽은 공지id 3개가 반환된다.")
    @Test
    void getNoticeListByCursor_success() {
        User user = createUser();
        Long userId = user.getId();

        List<Notice> notices = createNotices();

        List<Long> readIds = List.of(notices.get(0).getId(), notices.get(1).getId());
        readIds.forEach(id -> {
            noticeRedisService.createUserNoticeRead(userId, id);
        });

        // when
        SliceDTO<UserUnreadNoticeResponseDTO> result = userNoticeService.getNoticeListByCursor(null, 10, null, userId);

        // then
        assertThat(result.items()).hasSize(3);
        assertThat(result.hasNext()).isFalse();
        List<Long> resultIds = result.items().stream()
            .map(UserUnreadNoticeResponseDTO::id)
            .toList();
        assertThat(resultIds).doesNotContain(readIds.get(0), readIds.get(1));
    }

    @DisplayName("getNoticeListByCursor 성공: 읽은 목록이 레디스에는 없고 db에만 2개 있을 때 안읽은 공지id가 3개 반환된다.")
    @Test
    void getNoticeListByCursor_success_emptyRedis() {
        User user = createUser();
        Long userId = user.getId();

        List<Notice> notices = createNotices();

        List<Notice> readNotices = List.of(notices.get(0), notices.get(1));
        saveUserNoticeRead(readNotices, userId, user);

        // when
        SliceDTO<UserUnreadNoticeResponseDTO> result = userNoticeService.getNoticeListByCursor(null, 10, null, userId);

        // then
        assertThat(result.items()).hasSize(3);
        assertThat(result.hasNext()).isFalse();
        List<Long> resultIds = result.items().stream()
            .map(UserUnreadNoticeResponseDTO::id)
            .toList();
        assertThat(resultIds).doesNotContain(readNotices.get(0).getId(), readNotices.get(1).getId());
    }

    @DisplayName("getNoticeListByCursor 성공: 읽은 목록이 레디스에 2개 DB에 2개가 있어서 안읽은 목록이 총 1개가 반환된다.")
    @Test
    void getNoticeListByCursor_success_redisAndDb() {
        User user = createUser();
        Long userId = user.getId();

        List<Notice> notices = createNotices();


        for (int i = 0; i < 2; i++) {
            noticeRedisService.createUserNoticeRead(userId, notices.get(i).getId());
        }

        List<Notice> readNotices = List.of(notices.get(2), notices.get(3));
        saveUserNoticeRead(readNotices, userId, user);

        // when
        SliceDTO<UserUnreadNoticeResponseDTO> result = userNoticeService.getNoticeListByCursor(null, 10, null, userId);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        List<Long> resultIds = result.items().stream()
            .map(UserUnreadNoticeResponseDTO::id)
            .toList();
        assertThat(resultIds).doesNotContain(
            notices.get(0).getId(),
            notices.get(1).getId(),
            notices.get(2).getId(),
            notices.get(3).getId());
    }

    @DisplayName("getNoticeListByCursor 성공: 안읽은 공지가 (notice의 개수 - limit)개 만큼 더 남아 있음")
    @Test
    void getNoticeListByCursor_success_hasNextTrue() {
        // given
        User user = createUser();
        Long userId = user.getId();

        List<Notice> notices = createNotices();

        // when
        SliceDTO<UserUnreadNoticeResponseDTO> result = userNoticeService.getNoticeListByCursor(null, 3, null, userId);

        // then
        assertThat(result.items()).hasSize(3);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursorId()).isEqualTo(notices.get(2).getId());
        assertThat(result.nextCursorStandard()).isEqualTo(notices.get(2).getCreatedAt().toString());
        List<Long> resultIds = result.items().stream()
            .map(UserUnreadNoticeResponseDTO::id)
            .toList();
        assertThat(resultIds).doesNotContain(
            notices.get(0).getId(),
            notices.get(1).getId());
    }

    @DisplayName("getNoticeListByCursor 성공: 중간부터 안읽은 공지를 읽음")
    @Test
    void getNoticeListByCursor_success_cursorIdIsNotNull() {
        // given
        User user = createUser();
        Long userId = user.getId();

        List<Notice> notices = createNotices();

        // when
        SliceDTO<UserUnreadNoticeResponseDTO> result = userNoticeService.getNoticeListByCursor(notices.get(3).getId(), 2, notices.get(3).getCreatedAt().toString(), userId);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursorId()).isEqualTo(notices.get(1).getId());
        assertThat(result.nextCursorStandard()).isEqualTo(notices.get(1).getCreatedAt().toString());
        List<Long> resultIds = result.items().stream()
            .map(UserUnreadNoticeResponseDTO::id)
            .toList();
        assertThat(resultIds).doesNotContain(
            notices.get(0).getId(),
            notices.get(3).getId(),
            notices.get(4).getId());
    }

    @DisplayName("readAllNotice 성공: 모든 공지를 읽음 처리한다.")
    @Test
    void readAllNotice_success() {
        // given
        User mockUser = createUser();
        Long userId = mockUser.getId();
        createNotices();

        // when
        userNoticeService.readAllNotice(userId);

        // then
        String redisKey = "notice:read:user:" + userId;
        Set<Object> redisValues = redisTemplate.opsForSet().members(redisKey);

        assertThat(redisValues)
            .containsExactlyInAnyOrderElementsOf(
                noticeRepository.findAllNoticeIds().stream()
                    .map(String::valueOf)
                    .toList()
            );
    }

    private User createUser() {
        User user = User.builder()
            .email("notice@test.com")
            .nickname("테스터")
            .role(Role.TRAINEE)
            .loginType(LoginType.OAUTH)
            .status(Status.NORMAL)
            .signupAt(LocalDateTime.now())
            .lastLoginAt(LocalDateTime.now())
            .build();
        return userRepository.save(user);
    }

    private List<Notice> createNotices() {
        List<Notice> notices = (List<Notice>)IntStream.range(0, 5)
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
                .build())
            .toList();
        return noticeRepository.saveAllAndFlush(notices);
    }

    private void saveUserNoticeRead(List<Notice> readNotices, Long userId, User user) {
        readNotices.forEach(notice -> {
            if (!userNoticeReadRepository.existsByUserIdAndNoticeId(userId, notice.getId())) {
                userNoticeReadRepository.save(
                    UserNoticeRead.builder()
                        .user(user)
                        .notice(notice)
                        .build());
            }
        });
    }
}
