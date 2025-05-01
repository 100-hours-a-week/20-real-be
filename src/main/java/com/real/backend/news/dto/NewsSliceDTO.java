package com.real.backend.news.dto;

import java.util.List;

public record NewsSliceDTO(
    List<NewsListResponseDTO> items,
    String nextCursorStandard,
    Long nextCursorId,
    boolean hasNext) {}
