package com.real.backend.modules.news.service;

import static com.real.backend.common.util.CursorUtils.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.news.component.NewsFinder;
import com.real.backend.modules.news.domain.News;
import com.real.backend.modules.news.dto.NewsCommentRequestDTO;
import com.real.backend.modules.news.dto.NewsStressResponseDTO;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.exception.ForbiddenException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.news.domain.NewsComment;
import com.real.backend.modules.news.dto.NewsCommentListResponseDTO;
import com.real.backend.modules.news.repository.NewsCommentRepository;
import com.real.backend.modules.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsCommentService {

    private final NewsCommentRepository newsCommentRepository;
    private final UserFinder userFinder;
    private final NewsFinder newsFinder;
    private final PostRedisService postRedisService;

    public SliceDTO<NewsCommentListResponseDTO> getNewsCommentListByCursor(Long newsId, Long cursorId, String cursorStandard, int limit, Long currentUserId) {

        User currentUser = userFinder.getUser(currentUserId);
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
    public NewsStressResponseDTO deleteNewsComment(Long newsId, Long commentId, Long userId) {
        if (commentId == null) {
            throw new BadRequestException("필수 파라미터인 commentId를 받지 못했습니다.");
        }

        News news = newsFinder.getNews(newsId);
        NewsComment newsComment = newsCommentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("해당 id를 가진 댓글이 존재하지 않습니다."));

        if (!newsComment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글 작성자가 아닙니다.");
        }
        newsComment.delete();
        postRedisService.initCount("news", "comment", newsId, news.getCommentCount());
        postRedisService.decrement("news", "comment", newsId);
        return new NewsStressResponseDTO(newsComment.getId());
    }

    @Transactional
    public NewsStressResponseDTO createNewsComment(Long newsId, Long userId, NewsCommentRequestDTO newsCommentRequestDTO) {
        User user = userFinder.getUser(userId);
        News news = newsFinder.getNews(newsId);
        postRedisService.initCount("news", "comment", newsId, news.getCommentCount());
        Long commentCount = postRedisService.increment("news", "comment", newsId);

        return new NewsStressResponseDTO(newsCommentRepository.save(NewsComment.builder()
            .content(newsCommentRequestDTO.getContent())
            .user(user)
            .news(news)
            .build()).getId());
    }
}
