package com.real.backend.domain.news.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.domain.news.domain.News;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsResponseDTO {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String tag;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private boolean userLike;
    private String imageUrl;

    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime createdAt;

    public static NewsResponseDTO from(News news, boolean userLike) {
        return NewsResponseDTO.builder()
            .id(news.getId())
            .title(news.getTitle())
            .summary(news.getSummary())
            .content(news.getContent())
            .tag(news.getTag())
            .viewCount(news.getTotalViewCount())
            .likeCount(news.getLikeCount())
            .commentCount(news.getCommentCount())
            .userLike(userLike)
            .imageUrl(news.getImageUrl())
            .createdAt(news.getCreatedAt())
            .build();
    }
}
