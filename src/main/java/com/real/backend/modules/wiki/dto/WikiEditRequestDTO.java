package com.real.backend.modules.wiki.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiEditRequestDTO {
    private String html;
    private String ydoc;
}
