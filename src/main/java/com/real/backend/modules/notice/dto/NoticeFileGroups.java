package com.real.backend.modules.notice.dto;

import java.util.List;

public record NoticeFileGroups(
    List<NoticeFilesResponseDTO> files,
    List<NoticeFilesResponseDTO> images
) {}
