package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.user.domain.UserNoticeRead;
import com.real.backend.domain.notice.dto.NoticeCreateRequestDTO;
import com.real.backend.domain.notice.dto.NoticeFileGroups;
import com.real.backend.domain.notice.dto.NoticeInfoResponseDTO;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.repository.UserNoticeReadRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.infra.ai.dto.NoticeSummaryRequestDTO;
import com.real.backend.infra.ai.dto.NoticeSummaryResponseDTO;
import com.real.backend.infra.ai.service.NoticeAiService;
import com.real.backend.util.CursorUtils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeLikeService noticeLikeService;
    private final NoticeFileService noticeFileService;
    private final NoticeAiService noticeAiService;
    private final UserNoticeReadRepository userNoticeReadRepository;
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;

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
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);

        UserNoticeRead userNoticeRead = userNoticeReadRepository.findByUserAndNotice(user, notice).orElse(null);
        return userNoticeRead != null;

    }

    @Transactional
    public void createNotice(Long userId, NoticeCreateRequestDTO noticeCreateRequestDTO) throws
        JsonProcessingException {
        User user = userFinder.getUser(userId);

        NoticeSummaryRequestDTO noticeSummaryRequestDTO = new NoticeSummaryRequestDTO(noticeCreateRequestDTO.content(),
            noticeCreateRequestDTO.title());
        NoticeSummaryResponseDTO noticeSummaryResponseDTO = noticeAiService.makeSummary(noticeSummaryRequestDTO);

        noticeRepository.save(Notice.builder()
            .user(user)
            .title(noticeCreateRequestDTO.title())
            .content(noticeCreateRequestDTO.content())
            .summary(noticeSummaryResponseDTO.summary())
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

    @Transactional
    public void increaseViewCounts(Long noticeId) {
        Notice notice = noticeFinder.getNotice(noticeId);

        notice.increaseTotalViewCount();
        noticeRepository.save(notice);
    }

    @Transactional
    public void userReadNotice(Long noticeId, Long userId) {
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);

        if (!userNoticeReadRepository.findByUserAndNotice(user, notice).isPresent()) {
            userNoticeReadRepository.save(UserNoticeRead.builder().user(user).notice(notice).build());
        }
    }
}
