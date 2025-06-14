package com.real.backend.modules.wiki.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.real.backend.infra.redis.WikiRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiSyncService {

    private final WikiRedisService wikiRedisService;
    private final RedisTemplate<String, Object> redisTemplate;

    public void syncWiki() {
        List<Long> wikiIds = redisTemplate.keys("wiki:*").stream()
            .map(k -> k.substring("wiki:".length()))
            .map(Long::parseLong)
            .toList();

        Set<Long> failedIds = new HashSet<>();

        for (Long wikiId : wikiIds) {
            try {
                wikiRedisService.flushToDB(wikiId);
            } catch (Exception e) {
                failedIds.add(wikiId);
                log.warn("1차 flush 실패 - wikiId: {}", wikiId, e);
            }
        }

        retryFailedFlush(failedIds, 2);
    }

    private void retryFailedFlush(Set<Long> failedIds, int maxRetry) {
        int attempt = 1;

        while (attempt <= maxRetry && !failedIds.isEmpty()) {
            Set<Long> stillFailed = new HashSet<>();

            for (Long wikiId : failedIds) {
                try {
                    wikiRedisService.flushToDB(wikiId);
                } catch (Exception e) {
                    stillFailed.add(wikiId);
                    log.warn("{}차 flush 재시도 실패 - wikiId: {}", attempt + 1, wikiId, e);
                }
            }

            failedIds = stillFailed;
            attempt++;
        }

        if (!failedIds.isEmpty()) {
            log.error("flush 최종 실패 - wikiIds: {}", failedIds);
        }
    }
}
