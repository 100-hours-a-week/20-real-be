package com.real.backend.modules.wiki.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.modules.wiki.domain.Wiki;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiListResponseDTO {
    private Long id;
    private String title;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static WikiListResponseDTO of(Wiki wiki) {
        return WikiListResponseDTO.builder()
            .id(wiki.getId())
            .title(wiki.getTitle())
            .updatedAt(wiki.getUpdatedAt())
            .build();
    }
}
