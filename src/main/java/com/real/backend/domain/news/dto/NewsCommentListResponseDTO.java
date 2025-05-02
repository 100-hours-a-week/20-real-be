package com.real.backend.domain.news.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.real.backend.domain.news.domain.NewsComment;
import com.real.backend.domain.user.domain.User;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewsCommentListResponseDTO {
    private Long id;
    private boolean isAuthor;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private String profileUrl;

    public static NewsCommentListResponseDTO from(NewsComment newsComment, User author, User currentUser) {
        return NewsCommentListResponseDTO.builder()
            .id(newsComment.getId())
            .isAuthor(Objects.equals(author.getId(), currentUser.getId()))
            .nickname(author.getNickname())
            .content(newsComment.getContent())
            .createdAt(newsComment.getCreatedAt())
            .profileUrl(author.getProfileUrl())
            .build();
    }
}
