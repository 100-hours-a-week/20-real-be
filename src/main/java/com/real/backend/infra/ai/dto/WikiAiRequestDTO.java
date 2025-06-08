package com.real.backend.infra.ai.dto;

import com.real.backend.modules.wiki.domain.Wiki;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiAiRequestDTO {
    private String title;
    private String content;
    private String presignedUrl;

    public static WikiAiRequestDTO from(Wiki wiki, String presignedUrl) {
        return WikiAiRequestDTO.builder()
            .title(wiki.getTitle())
            .content(wiki.getHtml())
            .presignedUrl(presignedUrl)
            .build();
    }
}
