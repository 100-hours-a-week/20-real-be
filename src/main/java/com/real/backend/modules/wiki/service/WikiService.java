package com.real.backend.modules.wiki.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.wiki.domain.SearchMethod;
import com.real.backend.modules.wiki.domain.SortBy;
import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.modules.wiki.dto.WikiListResponseDTO;
import com.real.backend.modules.wiki.repository.WikiRepository;
import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.common.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiRepository wikiRepository;
    private final WikiRedisService wikiRedisService;

    @Transactional
    public Wiki createWiki(WikiCreateRequestDTO wikiCreateRequestDTO, String userName) {
        return wikiRepository.save(Wiki.builder()
            .title(wikiCreateRequestDTO.getTitle())
            .editorName(userName)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .ydoc(null)
            .build());
    }

    @Transactional(readOnly = true)
    public Wiki getWiki(String title, SearchMethod method) {
        if (SearchMethod.NORMAL.equals(method)) {
            Wiki wiki = wikiRedisService.getWikiById(title);
            if (wiki == null) {
                return wikiRepository.findByTitle(title).orElseThrow(() -> new NotFoundException("해당 제목을 가진 위키가 존재하지 않습니다."));
            }
            return wiki;
        } else {
            return getRandomWiki(wikiRepository.getAllId());
        }
    }

    @Transactional(readOnly = true)
    public Wiki getRandomWiki(List<Long> wikiIds) {
        Long randomId = wikiIds.get((int) (Math.random() * wikiIds.size()));
        return wikiRepository.findById(randomId).orElseThrow(() -> new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public SliceDTO<WikiListResponseDTO> getWikiListByCursor(Long cursorId, int limit, SortBy sort, String keyword, String cursorStandard) {
        String order = sort.toString().toLowerCase();

        if (!order.equals("latest") && !order.equals("title")) {
            throw new BadRequestException("sort 파라미터는 latest 또는 title 이어야 합니다.");
        }

        if (keyword != null && keyword.trim().isEmpty()) {
            throw new BadRequestException("keyword는 빈 문자열이 들어올 수 없습니다.");
        }

        Pageable pg = PageRequest.of(0, limit);
        boolean firstPage = (cursorId == null);

        Slice<Wiki> slice = switch (sort) {
            case LATEST -> firstPage
                ? wikiRepository.fetchLatestFirst(pg)
                : wikiRepository.fetchLatest(cursorId, pg, LocalDateTime.parse(cursorStandard));

            case TITLE -> firstPage
                ? wikiRepository.fetchTitleFirst(pg, keyword)
                : wikiRepository.fetchTitle(keyword, cursorId, pg);
        };

        List<Wiki> content = slice.getContent();
        boolean hasNext = slice.hasNext();
        List<Wiki> pageItems = content.size() > limit ? content.subList(0, limit) : content;

        // 다음 커서 계산
        String nextCursorStandard = null;
        Long nextCursorId = null;
        if (hasNext) {
            Wiki last = pageItems.get(pageItems.size() - 1);
            nextCursorId = last.getId();
            nextCursorStandard = order.equals("latest")
                ? last.getUpdatedAt().toString()
                : null;
        }

        List<WikiListResponseDTO> dtoList = pageItems.stream()
            .map(WikiListResponseDTO::of)
            .map(this::updateUpdatedAt)
            .toList();

        return new SliceDTO<>(dtoList, nextCursorStandard, nextCursorId, hasNext);
    }

    @Transactional
    public void deleteWiki(Long wikiId) {
        Wiki wiki = wikiRepository.findById(wikiId).orElseThrow(() -> new NotFoundException("해당 Id를 가진 위키가 존재하지 않습니다."));
        wiki.delete();
        wikiRepository.save(wiki);
    }

    public WikiListResponseDTO updateUpdatedAt(WikiListResponseDTO wikiListResponseDTO) {
        String updateAt = wikiRedisService.getUpdatedAtByWikiId(wikiListResponseDTO.getId());
        if (updateAt != null) {
            wikiListResponseDTO.updateUpdatedAt(LocalDateTime.parse(updateAt));
        }
        return wikiListResponseDTO;
    }
}
