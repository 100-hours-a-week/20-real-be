package com.real.backend.domain.news.dto;

import java.util.List;

public record NewsSliceDTO(
    List<NewsListResponseDTO> items,
    String nextCursorStandard,
    Long nextCursorId,
    boolean hasNext) {}
