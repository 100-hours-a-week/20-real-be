package com.real.backend.domain.wiki.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.real.backend.domain.wiki.domain.SearchMethod;
import com.real.backend.domain.wiki.domain.SortBy;
import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.domain.wiki.dto.WikiListResponseDTO;
import com.real.backend.domain.wiki.repository.WikiRepository;
import com.real.backend.exception.BadRequestException;
import com.real.backend.exception.NotFoundException;
import com.real.backend.util.dto.SliceDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiRepository wikiRepository;

    public Wiki createWiki(WikiCreateRequestDTO wikiCreateRequestDTO, String userName) {
        return wikiRepository.save(Wiki.builder()
            .title(wikiCreateRequestDTO.getTitle())
            .editorName(userName)
            .content(null)
            .build());
    }

    public Wiki getWiki(String title, SearchMethod method) {
        if (SearchMethod.NORMAL.equals(method)) {
            return wikiRepository.findByTitle(title).orElseThrow(() -> new NotFoundException("해당 제목을 가진 위키가 존재하지 않습니다."));
        } else {
            List<Long> wikiIds = wikiRepository.getAllId();
            Long randomId = wikiIds.get((int) (Math.random() * wikiIds.size()));
            return wikiRepository.findById(randomId).orElseThrow(() -> new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다."));
        }
    }

    public SliceDTO<WikiListResponseDTO> getWikiListByCursor(Long cursorId, int limit, SortBy sort, String keyword) {
        String order = sort.toString().toLowerCase();

        if (!order.equals("latest") && !order.equals("title")) {
            throw new BadRequestException("sort 파라미터는 latest 또는 title 이어야 합니다.");
        }

        Pageable pg = PageRequest.of(0, limit);
        boolean firstPage = (cursorId == null);

        Slice<Wiki> slice = switch (sort) {
            case LATEST -> firstPage
                ? wikiRepository.fetchLatestFirst(pg, keyword)
                : wikiRepository.fetchLatest(cursorId, pg, keyword);

            case TITLE -> firstPage
                ? wikiRepository.fetchTitleFirst(pg, keyword)
                : wikiRepository.fetchTitle(keyword, cursorId, pg);
        };

        List<Wiki> content = slice.getContent();
        boolean hasNext = slice.hasNext();
        List<Wiki> pageItems = content.size() > limit ? content.subList(0, limit) : content;

        // 다음 커서 계산
        Long nextCursorId = null;
        if (hasNext) {
            Wiki last = pageItems.get(pageItems.size() - 1);
            nextCursorId = last.getId();
        }

        List<WikiListResponseDTO> dtoList = pageItems.stream()
            .map(WikiListResponseDTO::of)
            .toList();

        return new SliceDTO<>(dtoList, null, nextCursorId, hasNext);
    }
}
