package com.real.backend.domain.wiki.dto;

import com.real.backend.domain.wiki.domain.Wiki;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiListResponseDTO {
    private Long id;
    private String title;

    public static WikiListResponseDTO of(Wiki wiki) {
        return WikiListResponseDTO.builder()
            .id(wiki.getId())
            .title(wiki.getTitle())
            .build();
    }
}
