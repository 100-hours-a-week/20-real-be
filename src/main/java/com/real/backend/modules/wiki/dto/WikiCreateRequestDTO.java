package com.real.backend.modules.wiki.dto;

import com.real.backend.common.aop.Sanitizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WikiCreateRequestDTO {

    @Sanitizer
    @Size(min = 1, max = 27)
    @NotBlank
    private String title;
}
