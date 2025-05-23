package com.real.backend.domain.news.dto;

import com.real.backend.global.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCreateRequestDTO {
    @NotBlank
    @Sanitizer
    @Size(min = 1, max = 26)
    private String title;

    @NotBlank
    @Sanitizer
    @Size(min = 1, max = 1024)
    private String content;
}
