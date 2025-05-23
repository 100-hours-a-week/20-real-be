package com.real.backend.domain.news.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.domain.news.domain.News;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NewsListResponseDTO{
    private Long id;
    private String title;
    private String tag;
    private Long commentCount;
    private Long todayViewCount;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static NewsListResponseDTO of(News news, Long todayViewCount, Long commentCount) {
        return NewsListResponseDTO.builder()
            .id(news.getId())
            .title(news.getTitle())
            .tag(news.getTag())
            .commentCount(commentCount)
            .todayViewCount(todayViewCount)
            .imageUrl(news.getImageUrl())
            .createdAt(news.getCreatedAt())
            .build();
    }
}
