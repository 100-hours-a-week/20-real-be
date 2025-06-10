package com.real.backend.modules.notice.dto;

import com.real.backend.modules.notice.domain.NoticeFile;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NoticeFilesResponseDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Integer fileSeqNo;

    public static NoticeFilesResponseDTO from(NoticeFile noticeFile) {
        return NoticeFilesResponseDTO.builder()
            .id(noticeFile.getId())
            .fileName(noticeFile.getName())
            .fileUrl(noticeFile.getFileUrl())
            .fileType(noticeFile.getType())
            .fileSeqNo(noticeFile.getFileSeqNo())
            .build();
    }
}
