package com.real.backend.modules.notice.service;

import static com.real.backend.common.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.notice.domain.Notice;
import com.real.backend.modules.notice.domain.NoticeComment;
import com.real.backend.modules.notice.dto.NoticeCommentRequestDTO;
import com.real.backend.modules.notice.component.NoticeFinder;
import com.real.backend.modules.notice.dto.NoticeCommentListResponseDTO;
import com.real.backend.modules.notice.dto.NoticeStressResponseDTO;
import com.real.backend.modules.notice.repository.NoticeCommentRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.common.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeCommentService {
    private final PostRedisService postRedisService;
    private final NoticeCommentRepository noticeCommentRepository;
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
    public NoticeStressResponseDTO deleteNoticeComment(Long noticeId, Long commentId, Long userId) {
        Notice notice = noticeFinder.getNotice(noticeId);
        NoticeComment noticeComment = noticeCommentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("해당 id를 가진 댓글이 존재하지 않습니다."));

        if (!noticeComment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글 작성자가 아닙니다.");
        }
        noticeComment.delete();
        postRedisService.initCount("notice", "comment", noticeId, notice.getCommentCount());
        postRedisService.decrement("notice", "comment", noticeId);
        return new NoticeStressResponseDTO(noticeComment.getId());
    }

    @Transactional
    public NoticeStressResponseDTO createNoticeComment(Long noticeId, Long userId, NoticeCommentRequestDTO noticeCommentRequestDTO) {
        User user = userFinder.getUser(userId);
        Notice notice = noticeFinder.getNotice(noticeId);
        postRedisService.initCount("notice", "comment", noticeId, notice.getCommentCount());
        postRedisService.increment("notice", "comment", noticeId);


        return new NoticeStressResponseDTO(noticeCommentRepository.save(NoticeComment.builder()
            .content(noticeCommentRequestDTO.getContent())
            .user(user)
            .notice(notice)
            .build()).getId());
    }
}
