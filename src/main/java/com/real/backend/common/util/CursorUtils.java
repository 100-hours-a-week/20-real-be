package com.real.backend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CursorUtils {

    @FunctionalInterface
    public interface SliceDtoBuilder<D, R> {
        R build(List<D> items, String nextCursor, Long nextCursorId, boolean hasNext);
    }

    public static Pageable buildPageable(int limit) {
        return PageRequest.of(0, limit);
    }

    public static <T, D, R> R toCursorDto(
        Slice<T> slice,
        int limit,
        Function<T, D> mapper,
        Function<T, String> cursorExtractor,
        Function<T, Long> idExtractor,
        SliceDtoBuilder<D, R> builder
    ) {
        List<T> content = slice.getContent();
        boolean hasNext = slice.hasNext();

        // limit+1 로 가져온 것 중 실제로 보낼 limit 개만
        List<T> pageItems = content.size() > limit
            ? content.subList(0, limit)
            : content;

        String nextCursor = null;
        Long nextCursorId = null;
        if (hasNext && !pageItems.isEmpty()) {
            T last = pageItems.get(pageItems.size() - 1);
            nextCursor   = cursorExtractor.apply(last);
            nextCursorId = idExtractor.apply(last);
        }

        List<D> dtoList = pageItems.stream()
            .map(mapper)
            .collect(Collectors.toList());

        return builder.build(dtoList, nextCursor, nextCursorId, hasNext);
    }
}
