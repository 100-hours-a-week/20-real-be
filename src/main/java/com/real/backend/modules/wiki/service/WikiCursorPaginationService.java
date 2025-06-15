package com.real.backend.modules.wiki.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.util.CursorUtils;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.modules.wiki.domain.SortBy;
import com.real.backend.modules.wiki.dto.WikiListResponseDTO;
import com.real.backend.modules.wiki.dto.WikiTitleCursor;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiCursorPaginationService {

    private static final String SORTED_KEY_LATEST = "wikis:sorted:latest";
    private static final String SORTED_KEY_TITLE = "wikis:sorted:title";

    private final StringRedisTemplate redisTemplate;
    private final WikiRepository wikiRepository;

    public SliceDTO<WikiListResponseDTO> getWikiListByCursor(Long cursorId, int limit, String cursorStandard, SortBy sort, String keyword){
        if (sort == SortBy.LATEST){
            return getWikiListWithLatest(limit, cursorStandard);
        } else if (sort == SortBy.TITLE) {
            return getWikiListWithTitle(limit, cursorStandard, keyword);
        } else {
            throw new BadRequestException("정렬 기준이 잘 못 들어왔습니다.");
        }
    }

    public SliceDTO<WikiListResponseDTO> getWikiListWithLatest(int limit, String cursorStandard) {
        double maxScore = (cursorStandard == null)
            ? Double.POSITIVE_INFINITY
            : LocalDateTime.parse(cursorStandard).toEpochSecond(ZoneOffset.UTC);

        long offset = (cursorStandard == null) ? 0 : 1;

        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
            .reverseRangeByScoreWithScores(
                SORTED_KEY_LATEST,
                Double.NEGATIVE_INFINITY,
                maxScore,
                offset,
                limit + 1);

        if (tuples == null || tuples.isEmpty()) {
            return CursorUtils.toCursorDto(
                List.of(), limit,
                Function.identity(),
                dto -> dto.getUpdatedAt().toString(),
                WikiListResponseDTO::getId,
                SliceDTO::of);
        }

        List<WikiListResponseDTO> dtos = tuples.stream().map(tuple -> {
            Long id = Long.valueOf(tuple.getValue());
            String title = wikiRepository.getWikiTitleById(id);
            LocalDateTime updatedAt = LocalDateTime.ofEpochSecond(tuple.getScore().longValue(), 0, ZoneOffset.UTC);
            return WikiListResponseDTO.from(id, title, updatedAt);
        }).collect(Collectors.toList());

        return CursorUtils.toCursorDto(
            dtos,
            limit,
            Function.identity(),
            dto -> dto.getUpdatedAt().toString(),
            WikiListResponseDTO::getId,
            SliceDTO::of
        );
    }

    public SliceDTO<WikiListResponseDTO> getWikiListWithTitle(int limit, String cursorStandard, String keyword) {
        boolean isFirstPage = (cursorStandard == null);

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> results = zSetOps.range(SORTED_KEY_TITLE, 0, -1);

        if (results == null || results.isEmpty()) {
            return CursorUtils.toCursorDto(
                List.of(),
                limit,
                this::mapToDtoByTitleZSet,
                WikiListResponseDTO::getTitle,
                WikiListResponseDTO::getId,
                SliceDTO::of
            );
        }

        List<WikiTitleCursor> filtered = results.stream()
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

    private WikiListResponseDTO mapToDtoByTitleZSet(WikiListResponseDTO dto) {
        return dto;
    }

    private WikiListResponseDTO mapToDto(WikiTitleCursor cursor) {
        String title = wikiRepository.getWikiTitleById(cursor.getId());
        LocalDateTime updatedAt = wikiRepository.getWikiUpdatedAtById(cursor.getId());

        return WikiListResponseDTO.from(cursor.getId(), title, updatedAt);
    }
}
