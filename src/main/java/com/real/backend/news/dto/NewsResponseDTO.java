package com.real.backend.news.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.news.domain.News;

import lombok.Builder;

@Builder
public record NewsResponseDTO(
    Long id,
    String title,
    String summary,
    String content,
    String tag,
    Long viewCount,
    Long likeCount,
    Long commentCount,
    boolean userLike,
    String imageUrl,
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
    LocalDateTime createdAt
) {
    // TODO userLike 가져오기
    public static NewsResponseDTO of(News news) {
        return NewsResponseDTO.builder()
            .id(news.getId())
            .title(news.getTitle())
            .summary(news.getSummary())
            .content(news.getContent())
            .tag(news.getTag())
            .viewCount(news.getTotalViewCount())
            .likeCount(news.getLikeCount())
            .commentCount(news.getCommentCount())
            .imageUrl(news.getImageUrl())
            .createdAt(news.getCreatedAt())
            .build();
    }
}
