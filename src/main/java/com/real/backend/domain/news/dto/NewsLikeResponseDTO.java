package com.real.backend.domain.news.dto;

import com.real.backend.domain.news.domain.NewsLike;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewsLikeResponseDTO {
    private Long id;
    private Boolean isActivated;

    public static NewsLikeResponseDTO from(NewsLike newsLike) {
        return NewsLikeResponseDTO.builder()
            .id(newsLike.getId())
            .isActivated(newsLike.getIsActivated())
            .build();
    }
}
