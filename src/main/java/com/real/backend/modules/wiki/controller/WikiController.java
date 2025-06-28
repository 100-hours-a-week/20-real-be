package com.real.backend.modules.wiki.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.common.response.DataResponse;
import com.real.backend.common.response.StatusResponse;
import com.real.backend.common.util.dto.SliceDTO;
import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.modules.wiki.domain.SearchMethod;
import com.real.backend.modules.wiki.domain.SortBy;
import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.modules.wiki.dto.WikiEditRequestDTO;
import com.real.backend.modules.wiki.dto.WikiEditV2RequestDTO;
import com.real.backend.modules.wiki.dto.WikiInfoRequestDTO;
import com.real.backend.modules.wiki.dto.WikiListResponseDTO;
import com.real.backend.modules.wiki.dto.WikiResponseDTO;
import com.real.backend.modules.wiki.service.WikiCursorPaginationService;
import com.real.backend.modules.wiki.service.WikiService;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WikiController {

    private final WikiService wikiService;
    private final WikiRedisService wikiRedisService;
    private final WikiCursorPaginationService wikiCursorPaginationService;

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
    @PutMapping(value = "/v1/wikis/{wikiId}")
    public StatusResponse updateWiki(
        @PathVariable Long wikiId,
        @CurrentSession Session session,
        @RequestBody WikiEditRequestDTO wikiEditRequestDTO
    ) {
        wikiRedisService.updateWiki(wikiId, wikiEditRequestDTO.getYdoc(), wikiEditRequestDTO.getHtml(), session.getUsername());
        return StatusResponse.of(200, "문서가 redis에 정상적으로 저장되었습니다.");
    }

    // 위키 편집 v2
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @PutMapping(value = "/v2/wikis/{wikiId}")
    public StatusResponse updateWiki(
        @PathVariable Long wikiId,
        @RequestBody WikiEditV2RequestDTO wikiEditRequestDTO
    ) {
        wikiRedisService.updateWiki(wikiId, wikiEditRequestDTO.getYdoc(), wikiEditRequestDTO.getHtml(), wikiEditRequestDTO.getEditorsId(), wikiEditRequestDTO.getApiKey());
        return StatusResponse.of(200, "문서가 redis에 정상적으로 저장되었습니다.");
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

    // 위키 id로 상세 검색
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/wikis/{wikiId}")
    public DataResponse<WikiResponseDTO> getWikiById(
        @PathVariable Long wikiId,
        @RequestBody WikiInfoRequestDTO wikiInfoRequestDTO
    ) {
        return DataResponse.of(wikiService.getWikiById(wikiId, wikiInfoRequestDTO.getApiKey()));
    }

    // 위키 목록
    @PreAuthorize("!hasAnyAuthority('OUTSIDER')")
    @GetMapping("/v1/wikis/list")
    public DataResponse<SliceDTO<WikiListResponseDTO>> getWikisList(
        @RequestParam(value = "cursorId", required = false) Long cursorId,
        @RequestParam(value = "cursorStandard", required = false) String cursorStandard,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam(value = "sort", required = false) SortBy sort,
        @RequestParam(value = "keyword", required = false) String keyword
    ) {
        SliceDTO<WikiListResponseDTO> wikiList = wikiCursorPaginationService.getWikiListByCursor(cursorId, limit, cursorStandard, sort, keyword);
        return DataResponse.of(wikiList);
    }

    // 위키 삭제
    @PreAuthorize("!hasAnyAuthority('OUTSIDER', 'TRAINEE')")
    @DeleteMapping("v1/wikis/{wikiId}")
    public StatusResponse deleteWiki(
        @PathVariable Long wikiId
    ) {
        wikiService.deleteWiki(wikiId);
        return StatusResponse.of(204, "위키가 성공적으로 삭제되었습니다.");
    }
}
