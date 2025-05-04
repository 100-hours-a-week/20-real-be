package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.UserNoticeRead;
import com.real.backend.domain.notice.dto.NoticeListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.notice.repository.UserNoticeReadRepository;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.exception.NotFoundException;
import com.real.backend.util.CursorUtils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserNoticeReadRepository userNoticeReadRepository;
    private final UserService userService;

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

    public Boolean getUserRead(Long userId, Long noticeId) {
        User user = userService.getUser(userId);
        Notice notice = getNotice(noticeId);

        UserNoticeRead userNoticeRead = userNoticeReadRepository.findByUserAndNotice(user, notice).orElse(null);
        return userNoticeRead != null;

    }

    public Notice getNotice(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() -> new NotFoundException("해당 id를 가진 공지가 존재하지 않습니다."));
    }
}
