package com.real.backend.domain.user.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.dto.UserUnreadNoticeResponseDTO;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.dto.LoginResponseDTO;
import com.real.backend.domain.user.domain.User;
import com.real.backend.util.CursorUtils;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserFinder userFinder;
    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public LoginResponseDTO getUserInfo(Long userId) {
        User user = userFinder.getUser(userId);
        return LoginResponseDTO.from(user);
    }

    @Transactional(readOnly = true)
    public SliceDTO<UserUnreadNoticeResponseDTO> getNoticeListByCursor(Long cursorId, int limit, String cursorStandard, Long userId) {

        Pageable pg = buildPageable(limit);

        Slice<Notice> slice = (cursorId == null || cursorStandard == null)
            ? noticeRepository.fetchUnreadLatestFirst(userId, pg)
            : noticeRepository.fetchUnreadLatest(LocalDateTime.parse(cursorStandard), cursorId, userId, pg);

        return CursorUtils.toCursorDto(
            slice,
            limit,
            notice -> new UserUnreadNoticeResponseDTO(
                notice.getId(),
                notice.getTitle()
            ),
            notice -> notice.getCreatedAt().toString(),
            Notice::getId,
            SliceDTO::new
        );
    }
}
