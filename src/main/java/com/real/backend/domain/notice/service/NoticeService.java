package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.UserNoticeRead;
import com.real.backend.domain.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.domain.notice.dto.NoticeFileGroups;
import com.real.backend.domain.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.notice.repository.UserNoticeReadRepository;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.exception.ForbiddenException;
import com.real.backend.util.CursorUtils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeLikeService noticeLikeService;
    private final NoticeFileService noticeFileService;
    private final UserNoticeReadRepository userNoticeReadRepository;
    private final UserService userService;
    private final NoticeFinder noticeFinder;

    @Transactional(readOnly = true)
    public SliceDTO<NoticeListResponseDTO> getNoticeListByCursor(Long cursorId, int limit, String cursorStandard, Long userId) {

        Pageable pg = buildPageable(limit);

        Slice<Notice> slice = (cursorId == null || cursorStandard == null)
            ? noticeRepository.fetchLatestFirst(pg)
            : noticeRepository.fetchLatest(LocalDateTime.parse(cursorStandard), cursorId, pg);

        return CursorUtils.toCursorDto(
            slice,
            limit,
            notice -> {
                Boolean userRead = getUserRead(userId, notice.getId());
                return NoticeListResponseDTO.from(
                    notice,
                    userRead,
                    notice.getUser().getNickname()
                );
            },
            notice -> notice.getCreatedAt().toString(),
            Notice::getId,
            SliceDTO::new
        );
    }

    @Transactional(readOnly = true)
    public Boolean getUserRead(Long userId, Long noticeId) {
        User user = userService.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);

        UserNoticeRead userNoticeRead = userNoticeReadRepository.findByUserAndNotice(user, notice).orElse(null);
        return userNoticeRead != null;

    }



    // TODO summary 구현
    @Transactional
    public void createNotice(Long userId, NoticeCreateRequestDTO noticeCreateRequestDTO) {
        User user = userService.getUser(userId);
        if (!user.getRole().toString().equals("STAFF")) {
            throw new ForbiddenException("운영진 외에는 접근할 수 없습니다.");
        }

        noticeRepository.save(Notice.builder()
            .user(user)
            .title(noticeCreateRequestDTO.title())
            .content(noticeCreateRequestDTO.content())
            //.summary()
            .tag(noticeCreateRequestDTO.tag())
            .originalUrl(noticeCreateRequestDTO.originalUrl())
            .totalViewCount(0L)
            .commentCount(0L)
            .likeCount(0L)
            .build());
    }

    @Transactional(readOnly = true)
    public NoticeInfoResponseDTO getNoticeById(Long noticeId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        NoticeFileGroups noticeFileGroups = noticeFileService.getNoticeFileGroups(notice);
        return NoticeInfoResponseDTO.from(notice, noticeLikeService.userIsLiked(noticeId, userId), noticeFileGroups.files(), noticeFileGroups.images());
    }
}
