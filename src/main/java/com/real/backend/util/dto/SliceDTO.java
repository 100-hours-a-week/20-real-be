package com.real.backend.util.dto;

import java.util.List;

public record SliceDTO<T>(
    List<T> items,
    String nextCursorStandard,
    Long nextCursorId,
    boolean hasNext
) {}
