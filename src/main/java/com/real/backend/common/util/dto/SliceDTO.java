package com.real.backend.common.util.dto;

import java.util.List;

public record SliceDTO<T>(
    List<T> items,
    String nextCursorStandard,
    Long nextCursorId,
    boolean hasNext
) {}
