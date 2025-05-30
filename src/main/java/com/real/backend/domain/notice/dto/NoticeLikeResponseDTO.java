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
    private Long userId;
    private Boolean isActivated;

    public static NoticeLikeResponseDTO from(NoticeLike noticeLike) {
        return NoticeLikeResponseDTO.builder()
            .id(noticeLike.getId())
            .userId(noticeLike.getUser().getId())
            .isActivated(noticeLike.getIsActivated())
            .build();
    }
    public static NoticeLikeResponseDTO of(Long userId, Boolean isActivated) {
        return NoticeLikeResponseDTO.builder()
            .userId(userId)
            .isActivated(isActivated)
            .build();
    }
}
