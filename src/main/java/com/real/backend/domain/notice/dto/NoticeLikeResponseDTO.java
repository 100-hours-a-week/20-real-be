package com.real.backend.domain.notice.dto;

import com.real.backend.domain.notice.domain.NoticeLike;

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

    public static NoticeLikeResponseDTO from(NoticeLike noticeLike) {
        return NoticeLikeResponseDTO.builder()
            .id(noticeLike.getId())
            .isActivated(noticeLike.getIsActivated())
            .build();
    }
}
