package com.real.backend.domain.wiki.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.domain.wiki.domain.Wiki;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WikiResponseDTO {
    private Long id;
    private String title;
    private byte[] ydoc;
    private String html;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static WikiResponseDTO from(Wiki wiki) {
        return WikiResponseDTO.builder()
            .id(wiki.getId())
            .title(wiki.getTitle())
            .ydoc(wiki.getYdoc())
            .html(wiki.getHtml())
            .updatedAt(wiki.getUpdatedAt())
            .build();
    }
}
