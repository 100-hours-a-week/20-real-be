package com.real.backend.domain.news.dto;

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

    public static NewsLikeResponseDTO of(Long newsId, Boolean isActivated) {
        return NewsLikeResponseDTO.builder()
            .id(newsId)
            .isActivated(isActivated)
            .build();
    }
}
