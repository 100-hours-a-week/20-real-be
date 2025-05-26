package com.real.backend.domain.wiki.controller;

import java.io.IOException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.wiki.domain.SearchMethod;
import com.real.backend.domain.wiki.domain.SortBy;
import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.domain.wiki.dto.WikiListResponseDTO;
import com.real.backend.domain.wiki.dto.WikiResponseDTO;
import com.real.backend.domain.wiki.service.WikiService;
import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.response.DataResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;
import com.real.backend.util.dto.SliceDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WikiController {

    private final WikiService wikiService;
    private final WikiRedisService wikiRedisService;

    // 새로운 위키 생성
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PostMapping("/v1/wikis")
    public DataResponse<WikiResponseDTO> createWiki(
        @RequestBody WikiCreateRequestDTO wikiCreateRequestDTO,
        @CurrentSession Session session
    ) {
        Wiki wiki = wikiService.createWiki(wikiCreateRequestDTO, session.getUsername());
        WikiResponseDTO wikiResponseDTO = WikiResponseDTO.from(wiki);
        return DataResponse.of(wikiResponseDTO);
    }

    // 위키 편집
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PutMapping("/v1/wikis/{wikiId}")
    public void updateWiki(
        @PathVariable Long wikiId,
        @CurrentSession Session session,
        HttpServletRequest request
    ) throws IOException {

        wikiRedisService.updateWiki(wikiId, request.getInputStream().readAllBytes(), session.getUsername());
    }

    // 위키 상세
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/wikis")
    public DataResponse<WikiResponseDTO> getWiki(
        @RequestParam(required = false) String title,
        @RequestParam(defaultValue = "NORMAL") SearchMethod method
    ) {
        Wiki wiki = wikiService.getWiki(title, method);
        WikiResponseDTO wikiResponseDTO = WikiResponseDTO.from(wiki);
        return DataResponse.of(wikiResponseDTO);
    }

    // 위키 목록
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/wikis/list")
    public DataResponse<SliceDTO<WikiListResponseDTO>> getWikisList(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam(value = "sort", required = false) SortBy sort,
        @RequestParam String keyword
    ) {
        SliceDTO<WikiListResponseDTO> wikiList = wikiService.getWikiListByCursor(cursorId, limit, sort, keyword);
        return DataResponse.of(wikiList);
    }
}
