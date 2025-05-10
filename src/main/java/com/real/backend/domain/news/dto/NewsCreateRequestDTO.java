package com.real.backend.domain.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsCreateRequestDTO (
    @NotBlank
    @Size(min = 1, max = 26)
    String title,

    @NotBlank
    @Size(min = 1, max = 1024)
    String content
){
}
