package com.real.backend.modules.notice.dto;

import com.real.backend.common.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCreateRequestDTO {
	@NotBlank
	@Sanitizer
	private String title;

	@NotBlank
	@Sanitizer
	private String content;

	@NotBlank
	private String tag;

	@NotBlank
	private String userName;

	@Sanitizer
	private String originalUrl;
	private String platform;
	private String createdAt;
}
