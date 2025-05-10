package com.real.backend.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticePasteRequestDTO {
	@NotBlank
	@Size(min = 1, max = 26, message = "제목은 27자 이상 작성할 수 없습니다.")
	private String title;

	@NotBlank
	@Size(min = 1, max = 20000, message = "내용을 2만자 이상 작성할 수 없습니다.")
	private String content;

	@NotBlank
	private String tag;

	@NotBlank
	private String userName;

	private String originalUrl;
	private String platform;
	private String createdAt;
}
