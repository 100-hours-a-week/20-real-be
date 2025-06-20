package com.real.backend.modules.news.dto;

import com.real.backend.common.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewsCommentRequestDTO {

    @NotBlank
    @Sanitizer
    @Size(min = 1, max = 500)
    private String content;
}
