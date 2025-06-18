package com.real.backend.infra.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WikiNewsAiResponseDTO {
    private String headline;
    private String summary;
    private String news;
    private String imageUrl;
    private Boolean isCompleted;

    public void addWikiLink(String link) {
        this.news += "\n" + "원본 위키: " +link;
    }
}
