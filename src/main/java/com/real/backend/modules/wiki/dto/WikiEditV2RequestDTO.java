package com.real.backend.modules.wiki.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiEditV2RequestDTO {
    private String html;
    private String ydoc;
    private List<Long> editorsId;
    private String apiKey;
}
