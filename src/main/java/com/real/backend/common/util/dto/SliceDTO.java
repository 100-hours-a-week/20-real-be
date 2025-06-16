package com.real.backend.common.util.dto;

import java.util.List;

public record SliceDTO<T>(
    List<T> items,
    String nextCursorStandard,
    Long nextCursorId,
    boolean hasNext
) {
    public static <T> SliceDTO<T> of(List<T> items, String nextCursor, Long nextCursorId, boolean hasNext) {
        return new SliceDTO<>(items, nextCursor, nextCursorId, hasNext);
    }

}
