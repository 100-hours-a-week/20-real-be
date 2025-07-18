package com.real.backend.modules.notice.service;

import static com.real.backend.common.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.dto.NoticeListResponseDTO;
import com.real.backend.modules.notice.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeCursorPaginationService {

    private final NoticeRepository noticeRepository;
    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    public SliceDTO<NoticeListResponseDTO> getNoticeListByCursor(Long cursorId, int limit, String cursorStandard, Long userId, String keyword) {

        Pageable pg = buildPageable(limit);
        Slice<Notice> slice;

        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword) {
            slice = (cursorId == null || cursorStandard == null)
                ? noticeRepository.searchByKeywordFirst(keyword, pg)
                : noticeRepository.searchByKeyword(keyword, LocalDateTime.parse(cursorStandard), cursorId, pg);
        } else {
            slice = (cursorId == null || cursorStandard == null)
                ? noticeRepository.fetchLatestFirst(pg)
                : noticeRepository.fetchLatest(LocalDateTime.parse(cursorStandard), cursorId, pg);
        }

        return CursorUtils.toCursorDto(
            slice,
            limit,
            notice -> {
                Boolean userRead = noticeRedisService.getUserRead(userId, notice.getId());
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
}
