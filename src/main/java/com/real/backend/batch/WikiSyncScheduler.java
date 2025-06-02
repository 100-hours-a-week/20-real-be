package com.real.backend.batch;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.infra.redis.WikiRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WikiSyncScheduler {

    private final PostRedisService postRedisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WikiRedisService wikiRedisService;

    @Transactional
    @Scheduled(cron = "0 */30 * * * *")
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
