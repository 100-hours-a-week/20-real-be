package com.real.backend.domain.notice.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.domain.Notice;
import com.real.backend.domain.notice.domain.NoticeComment;
import com.real.backend.domain.notice.dto.NoticeCommentRequestDTO;
import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.dto.NoticeCommentListResponseDTO;
import com.real.backend.domain.notice.repository.NoticeCommentRepository;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.exception.BadRequestException;
import com.real.backend.exception.ForbiddenException;
import com.real.backend.exception.NotFoundException;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeCommentService {
    private final NoticeCommentRepository noticeCommentRepository;
    private final NoticeRepository noticeRepository;
    private final UserFinder userFinder;
    private final NoticeFinder noticeFinder;

    @Transactional(readOnly = true)
    public SliceDTO<NoticeCommentListResponseDTO> getNoticeCommentListByCursor(Long noticeId, Long cursorId, String cursorStandard, int limit, Long currentUserId) {

        User currentUser = userFinder.getUser(currentUserId);
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

    @Transactional
    public void deleteNoticeComment(Long noticeId, Long commentId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        NoticeComment noticeComment = noticeCommentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("해당 id를 가진 댓글이 존재하지 않습니다."));

        if (!noticeComment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글 작성자가 아닙니다.");
        }
        noticeComment.delete();
        notice.decreaseCommentCount();
        noticeRepository.save(notice);
    }

    // TODO XSS 필터링
    @Transactional
    public void createNoticeComment(Long noticeId, Long userId, NoticeCommentRequestDTO noticeCommentRequestDTO) {
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);
        notice.increaseCommentCount();

        noticeCommentRepository.save(NoticeComment.builder()
            .content(noticeCommentRequestDTO.getContent())
            .user(user)
            .notice(notice)
            .build());
        noticeRepository.save(notice);
    }
}
