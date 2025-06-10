package com.real.backend.modules.wiki.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.real.backend.infra.redis.WikiRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiSyncService {

    private final WikiRedisService wikiRedisService;
    private final RedisTemplate<String, Object> redisTemplate;

    public void syncWiki() {
        List<Long> wikiIds = redisTemplate.keys("wiki:*").stream()
            .map(k -> (k.substring(("keys:").length())))
            .map(Long::parseLong)
            .toList();

        for (Long wikiId : wikiIds) {
            wikiRedisService.flushToDB(wikiId);
        }
    }
}
