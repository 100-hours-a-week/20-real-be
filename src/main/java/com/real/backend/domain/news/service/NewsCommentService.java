package com.real.backend.domain.news.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.real.backend.util.dto.SliceDTO;
import com.real.backend.domain.news.domain.NewsComment;
import com.real.backend.domain.news.dto.NewsCommentListResponseDTO;
import com.real.backend.domain.news.repository.NewsCommentRepository;
import com.real.backend.domain.user.domain.User;
import com.real.backend.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsCommentService {

    private final NewsCommentRepository newsCommentRepository;
    private final UserService userService;

    public SliceDTO<NewsCommentListResponseDTO> getNewsCommentListByCursor(Long newsId, Long cursorId, String cursorStandard, int limit, Long currentUserId) {

        User currentUser = userService.getUser(currentUserId);
        Pageable pg = buildPageable(limit);

        Slice<NewsComment> slice = (cursorId == null || cursorStandard == null)
            ? newsCommentRepository.fetchLatestFirst(pg, newsId)
            : newsCommentRepository.fetchLatest(LocalDateTime.parse(cursorStandard), cursorId, pg, newsId);

        return toCursorDto(
            slice,
            limit,
            comment -> NewsCommentListResponseDTO.from(
                comment,
                comment.getUser(),    // 댓글 작성자
                currentUser             // 현재 로그인 유저
            ),                          // News → DTO
            newsComment -> String.valueOf(newsComment.getCreatedAt()), // cursor 값
            NewsComment::getId,                                       // cursor ID
            SliceDTO<NewsCommentListResponseDTO>::new                                  // 최종 DTO 빌더
        );
    }
}
