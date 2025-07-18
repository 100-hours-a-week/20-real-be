package com.real.backend.modules.notice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.real.backend.modules.notice.domain.Notice;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NoticeInfoResponseDTO {
    private Long id;
    private String title;
    private String author;
    private String platform;
    private String content;
    private String summary;
    private String tag;
    private Long likeCount;
    private Long commentCount;
    private String originalUrl;
    private Boolean userLike;
    @JsonFormat(pattern = "yyyy.MM.dd HH:mm:ss")
    private LocalDateTime createdAt;
    private List<NoticeFilesResponseDTO> files;
    private List<NoticeFilesResponseDTO> images;

    public static NoticeInfoResponseDTO from(
        Notice notice,
        Boolean userLike,
        Long likeCount,
        Long commentCount,
        List<NoticeFilesResponseDTO> files,
        List<NoticeFilesResponseDTO> images) {

        return NoticeInfoResponseDTO.builder()
            .id(notice.getId())
            .title(notice.getTitle())
            .author(notice.getUser().getNickname())
            .platform(notice.getPlatform())
            .content(notice.getContent())
            .summary(notice.getSummary())
            .tag(notice.getTag())
            .likeCount(likeCount)
            .commentCount(commentCount)
            .originalUrl(notice.getOriginalUrl())
            .userLike(userLike)
            .createdAt(notice.getCreatedAt())
            .images(images)
            .files(files)
            .build();
    }
}
