package com.real.backend.domain.notice.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.real.backend.domain.notice.domain.NoticeComment;
import com.real.backend.domain.user.domain.User;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NoticeCommentListResponseDTO {
    private Long id;
    private Boolean isAuthor;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private String profileUrl;

    public static NoticeCommentListResponseDTO from(NoticeComment noticeComment, User author, User currentUser) {
        return NoticeCommentListResponseDTO.builder()
            .id(noticeComment.getId())
            .isAuthor(Objects.equals(author.getId(), currentUser.getId()))
            .nickname(author.getNickname())
            .content(noticeComment.getContent())
            .createdAt(noticeComment.getCreatedAt())
            .profileUrl(author.getProfileUrl())
            .build();
    }
}
