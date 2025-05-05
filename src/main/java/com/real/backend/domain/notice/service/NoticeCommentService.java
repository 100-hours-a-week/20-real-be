package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.real.backend.domain.notice.domain.NoticeComment;
import com.real.backend.domain.notice.dto.NoticeCommentListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeCommentRepository;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.service.UserService;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeCommentService {
    private final NoticeCommentRepository noticeCommentRepository;
    private final UserService userService;

    public SliceDTO<NoticeCommentListResponseDTO> getNoticeCommentListByCursor(Long noticeId, Long cursorId, String cursorStandard, int limit, Long currentUserId) {

        User currentUser = userService.getUser(currentUserId);
        Pageable pg = buildPageable(limit);

        Slice<NoticeComment> slice = (cursorId == null || cursorStandard == null)
            ? noticeCommentRepository.fetchLatestFirst(pg, noticeId)
            : noticeCommentRepository.fetchLatest(LocalDateTime.parse(cursorStandard), cursorId, pg, noticeId);

        return toCursorDto(
            slice,
            limit,
            comment -> NoticeCommentListResponseDTO.from(
                comment,
                comment.getUser(),    // 댓글 작성자
                currentUser             // 현재 로그인 유저
            ),                          // Notice → DTO
            noticeComment -> String.valueOf(noticeComment.getCreatedAt()), // cursor 값
            NoticeComment::getId,                                       // cursor ID
            SliceDTO<NoticeCommentListResponseDTO>::new                                  // 최종 DTO 빌더
        );
    }
}
