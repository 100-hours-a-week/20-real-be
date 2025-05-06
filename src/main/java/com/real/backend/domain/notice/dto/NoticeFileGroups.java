package com.real.backend.domain.notice.dto;

import java.util.List;

public record NoticeFileGroups(
    List<NoticeFilesResponseDTO> files,
    List<NoticeFilesResponseDTO> images
) {}
