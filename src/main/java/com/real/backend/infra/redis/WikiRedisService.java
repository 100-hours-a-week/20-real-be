package com.real.backend.infra.redis;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void updateWiki(Long wikiId, byte[] content, String username) {
        redisTemplate.opsForValue().set("wiki:content:"+wikiId, content);
        redisTemplate.opsForValue().set("wiki:editor_name:"+wikiId, username);
        redisTemplate.opsForValue().set("wiki:updated_at:"+wikiId, LocalDateTime.now().toString());
    }
}
