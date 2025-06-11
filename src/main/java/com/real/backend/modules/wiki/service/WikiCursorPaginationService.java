package com.real.backend.modules.wiki.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.real.backend.common.exception.NotFoundException;
import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.wiki.dto.WikiListResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiCursorPaginationService {

    private static final String SORTED_KEY = "wikis:sorted:latest";
    private final StringRedisTemplate redisTemplate;

    public SliceDTO<WikiListResponseDTO> getWikiListByCursor(
        Long cursorId,
        int limit,
        String cursorStandard
    ) {
        double maxScore = (cursorStandard == null)
            ? Double.POSITIVE_INFINITY
            : LocalDateTime.parse(cursorStandard)
            .toEpochSecond(ZoneOffset.UTC);

        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeByScoreWithScores(
                SORTED_KEY,
                Double.NEGATIVE_INFINITY,
                maxScore,
                0,
                limit + 1
            );

        if (tuples == null || tuples.isEmpty()) {
            throw new NotFoundException("위키 목록을 불러올 수 없습니다.");
        }

        List<Long> idList = tuples.stream()
            .limit(limit)
            .map(ZSetOperations.TypedTuple::getValue)
            .map(Long::valueOf)
            .collect(Collectors.toList());

        return CursorUtils.toCursorDto(
            idList,
            limit,
            id -> {
                Map<Object, Object> wikiMap = redisTemplate.opsForHash().entries("wiki:" + id);
                return WikiListResponseDTO.from(
                    id,
                    (String) wikiMap.get("title"),
                    LocalDateTime.parse((String) wikiMap.get("updated_at"))
                    );
            },
            id -> {
                Map<Object, Object> wikiMap = redisTemplate.opsForHash().entries("wiki:" + id);
                return (String) wikiMap.get("updated_at");
            },
            Function.identity(),
            SliceDTO::new
        );
    }
}
