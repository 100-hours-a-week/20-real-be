package com.real.backend.domain.wiki.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WikiEditRequestDTO {
    private String html;
}
