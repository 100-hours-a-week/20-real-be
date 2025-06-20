package com.real.backend.modules.notice.dto;

import com.real.backend.common.aop.Sanitizer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeCommentRequestDTO {
	@Sanitizer
	@NotBlank
	@Size(min = 1, max = 500)
	private String content;

}
