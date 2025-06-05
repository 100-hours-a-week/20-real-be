package com.real.backend.modules.news.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.modules.news.domain.NewsComment;
import com.real.backend.modules.user.domain.User;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewsCommentListResponseDTO {
    private Long id;
    private Boolean isAuthor;
    private String nickname;
    private String content;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
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
