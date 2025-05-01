package com.real.backend.domain.news.service;

import static com.real.backend.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.dto.NewsCommentRequestDTO;
import com.real.backend.exception.BadRequestException;
import com.real.backend.exception.ForbiddenException;
import com.real.backend.exception.NotFoundException;
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
    private final NewsService newsService;

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
    @Transactional
    public void deleteNewsComment(Long newsId, Long commentId, Long userId) {
        if (commentId == null) {
            throw new BadRequestException("필수 파라미터인 commentId를 받지 못했습니다.");
        }

        NewsComment newsComment = newsCommentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("해당 id를 가진 뉴스가 존재하지 않습니다."));

        if (!newsComment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글 작성자가 아닙니다.");
        }
        newsComment.delete();
    }

    @Transactional
    public void createNewsComment(Long newsId, Long userId, NewsCommentRequestDTO newsCommentRequestDTO) {
        User user = userService.getUser(userId);
        News news = newsService.getNews(newsId);

        newsCommentRepository.save(NewsComment.builder()
            .content(newsCommentRequestDTO.content())
            .user(user)
            .news(news)
            .build());
    }
}
