package com.real.backend.domain.wiki.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.response.StatusResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WikiFlushController {

    private final WikiRedisService wikiRedisService;

    @PostMapping("/v1/wikis/{wikiId}/flush")
    public StatusResponse flushToDB(
        @PathVariable Long wikiId
    ) {
        wikiRedisService.flushToDB(wikiId);
        return StatusResponse.of(200, "데이터가 정상적으로 db에 업로드 됐습니다.");
    }
}
