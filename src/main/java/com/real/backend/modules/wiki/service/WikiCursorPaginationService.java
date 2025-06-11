package com.real.backend.modules.wiki.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.wiki.domain.SortBy;
import com.real.backend.modules.wiki.dto.WikiListResponseDTO;
import com.real.backend.modules.wiki.dto.WikiTitleCursor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiCursorPaginationService {

    private static final String SORTED_KEY = "wikis:sorted:latest";
    private final StringRedisTemplate redisTemplate;

    public SliceDTO<WikiListResponseDTO> getWikiListByCursor(Long cursorId, int limit, String cursorStandard, SortBy sort, String keyword){
        if (sort == SortBy.LATEST){
            return getWikiListWithLatest(limit, cursorStandard);
        } else if (sort == SortBy.TITLE) {
            return searchWikiByTitleKeyword(keyword, cursorStandard, limit);
        } else {
            throw new BadRequestException("정렬 기준이 잘 못 들어왔습니다.");
        }
    }

    public SliceDTO<WikiListResponseDTO> searchWikiByTitleKeyword(String keyword, String cursorStandard, int limit) {
        boolean isFirstPage = (cursorStandard == null);
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        String redisKey = "wikis:title:index";

        Set<String> allTitles = zSetOps.range(redisKey, 0, -1); // 전체 range

        if (allTitles == null || allTitles.isEmpty()) {
            return CursorUtils.toCursorDto(
                List.of(), limit,
                this::mapToDto,
                WikiTitleCursor::getTitle,
                WikiTitleCursor::getId,
                SliceDTO::of
            );
        }

        List<WikiTitleCursor> filtered = allTitles.stream()
            .map(val -> {
                String[] parts = val.split(":");
                return new WikiTitleCursor(parts[0], Long.valueOf(parts[1]));
            })
            .filter(e -> e.getTitle().contains(keyword))
            .filter(e -> isFirstPage || e.getTitle().compareTo(cursorStandard) > 0) // 커서 처리
            .sorted(Comparator.comparing(WikiTitleCursor::getTitle))
            .collect(Collectors.toList());

        return CursorUtils.toCursorDto(
            filtered, limit,
            this::mapToDto,
            WikiTitleCursor::getTitle,
            WikiTitleCursor::getId,
            SliceDTO::of
        );
    }


    public SliceDTO<WikiListResponseDTO> getWikiListWithLatest(int limit, String cursorStandard) {
        double maxScore = (cursorStandard == null)
            ? Double.POSITIVE_INFINITY
            : LocalDateTime.parse(cursorStandard)
            .toEpochSecond(ZoneOffset.UTC);
        long offset = cursorStandard == null ? 0 : 1;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeByScoreWithScores(
                SORTED_KEY,
                Double.NEGATIVE_INFINITY,
                maxScore,
                offset,
                limit + 1
            );

        if (tuples == null || tuples.isEmpty()) {
            throw new NotFoundException("위키 목록을 불러올 수 없습니다.");
        }

        List<Long> idList = tuples.stream()
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

    private WikiListResponseDTO mapToDto(WikiTitleCursor cursor) {
        String redisKey = "wiki:" + cursor.getId();
        String title = redisTemplate.opsForHash().get(redisKey, "title").toString();
        String updatedAt = redisTemplate.opsForHash().get(redisKey, "updated_at").toString();

        return WikiListResponseDTO.from(cursor.getId(), title, LocalDateTime.parse(updatedAt));
    }
}
