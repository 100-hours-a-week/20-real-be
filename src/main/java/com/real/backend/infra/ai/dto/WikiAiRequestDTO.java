package com.real.backend.infra.ai.dto;

import com.real.backend.domain.wiki.domain.Wiki;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiAiRequestDTO {
    private String title;
    private String content;

    public static WikiAiRequestDTO from(Wiki wiki) {
        return WikiAiRequestDTO.builder()
            .title(wiki.getTitle())
            .content(wiki.getHtml())
            .build();
    }
}
