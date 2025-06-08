package com.real.backend.modules.notice.dto;

import com.real.backend.common.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeCreateRequestDTO {
	@NotBlank
	@Sanitizer
	@Size(min = 1, max = 26)
	private String title;

	@NotBlank
	@Sanitizer
	@Size(min = 1, max = 20000)
	private String content;

	@NotBlank
	private String tag;

	@Sanitizer
	private String originalUrl;
}
