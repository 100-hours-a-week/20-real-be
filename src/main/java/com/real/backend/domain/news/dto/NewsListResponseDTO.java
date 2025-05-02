package com.real.backend.domain.news.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.domain.news.domain.News;
import lombok.Builder;

@Builder
public record NewsListResponseDTO(
    Long id,
    String title,
    Long commentCount,
    Long todayViewCount,
    String imageUrl,
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    LocalDateTime createdAt) {
    public static NewsListResponseDTO of(News news) {
        return NewsListResponseDTO.builder()
                    .id(news.getId())
                    .title(news.getTitle())
                    .commentCount(news.getCommentCount())
                    .todayViewCount(news.getTodayViewCount())
                    .imageUrl(news.getImageUrl())
                    .createdAt(news.getCreatedAt())
                    .build();
    }
}
