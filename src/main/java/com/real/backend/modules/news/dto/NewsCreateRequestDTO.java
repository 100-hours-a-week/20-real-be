package com.real.backend.modules.news.dto;

import com.real.backend.common.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCreateRequestDTO {
    @NotBlank
    @Sanitizer
    @Size(min = 1, max = 50)
    private String title;

    @NotBlank
    @Sanitizer
    @Size(min = 1, max = 5000)
    private String content;
}
