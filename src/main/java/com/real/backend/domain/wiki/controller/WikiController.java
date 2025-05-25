package com.real.backend.domain.wiki.controller;

import java.io.IOException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.domain.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.domain.wiki.service.WikiService;
import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.response.StatusResponse;
import com.real.backend.security.CurrentSession;
import com.real.backend.security.Session;

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
    public StatusResponse createWiki(
        @RequestBody WikiCreateRequestDTO wikiCreateRequestDTO,
        @CurrentSession Session session
    ) {
        wikiService.createWiki(wikiCreateRequestDTO, session.getUsername());
        return StatusResponse.of(201, "위키가 성공적으로 생성되었습니다.");
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
}
