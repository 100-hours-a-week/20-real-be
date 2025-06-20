package com.real.backend.modules.user.service;

import static com.real.backend.common.util.CursorUtils.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.modules.notice.component.NoticeFinder;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.repository.NoticeRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.dto.UserUnreadNoticeResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserNoticeService {
    private final NoticeFinder noticeFinder;
    private final UserFinder userFinder;
    private final NoticeRepository noticeRepository;
    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    public SliceDTO<UserUnreadNoticeResponseDTO> getNoticeListByCursor(Long cursorId, int limit, String cursorStandard, Long userId) {
        List<Long> readIds = noticeRedisService.getUserReadList(userId);
        List<Notice> unreadAccum = new ArrayList<>();

        LocalDateTime dbCursorTime = cursorStandard != null ? LocalDateTime.parse(cursorStandard) : null;
        Long dbCursorId = cursorId;
        boolean hasMoreDb = true;

        while (unreadAccum.size() <= limit && hasMoreDb) {
            Pageable pg = buildPageable(limit + 1);
            Slice<Notice> slice = (dbCursorTime == null)
                ? noticeRepository.fetchUnreadLatestFirst(userId, pg)
                : noticeRepository.fetchUnreadLatest(dbCursorTime, dbCursorId, userId, pg);

            hasMoreDb = slice.hasNext();
            List<Notice> content = slice.getContent();

            if (!content.isEmpty()) {
                Notice lastDbNotice = content.get(content.size() - 1);
                dbCursorTime = lastDbNotice.getCreatedAt();
                dbCursorId = lastDbNotice.getId();
            }

            for (Notice n : content) {
                if (readIds == null || !readIds.contains(n.getId())) {
                    unreadAccum.add(n);
                }
            }
        }

        return CursorUtils.toCursorDto(
            unreadAccum,
            limit,
            notice -> new UserUnreadNoticeResponseDTO(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt()
            ),
            notice -> notice.getCreatedAt().toString(),
            Notice::getId,
            SliceDTO::new
        );
    }

    @Transactional(readOnly = true)
    public void readAllNotice(Long userId) {
        userFinder.getUser(userId);

        List<Long> noticeIds = noticeRepository.findAllNoticeIds();
        noticeRedisService.userNoticeReadAll(noticeIds, userId);
    }

    @Transactional
    public void userReadNotice(Long noticeId, Long userId) {
        userFinder.getUser(userId);
        noticeFinder.getNotice(noticeId);

        noticeRedisService.createUserNoticeRead(userId, noticeId);
    }
}
