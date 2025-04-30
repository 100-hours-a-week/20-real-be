package com.real.backend.news.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.news.domain.News;
import lombok.Builder;

@Builder
public record NewsListResponseDTO(
    long id,
    String title,
    long commentCount,
    long todayViewCount,
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
