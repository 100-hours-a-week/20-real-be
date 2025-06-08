package com.real.backend.modules.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeLikeResponseDTO {
    private Long id;
    private Boolean isActivated;

    public static NoticeLikeResponseDTO of(Long noticeId, Boolean isActivated) {
        return NoticeLikeResponseDTO.builder()
            .id(noticeId)
            .isActivated(isActivated)
            .build();
    }
}
