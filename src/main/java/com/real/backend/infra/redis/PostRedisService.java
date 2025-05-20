package com.real.backend.infra.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final RedisTemplate<Object, Object> redisTemplate;

    private String buildKey(String domain, String type, Long id) {
        return domain + ":" + type + ":" + id;  // ex) news:view:12
    }

    public void initCount(String domain, String type, Long id, Long value) {
        String key = buildKey(domain, type, id);
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public Long increment(String domain, String type, Long id) {
        return redisTemplate.opsForValue().increment(buildKey(domain, type, id));
    }

    public Long getCount(String domain, String type, Long id) {
        Object val = redisTemplate.opsForValue().get(buildKey(domain, type, id));
        return val == null ? 0L : Long.parseLong(val.toString());
    }

    public void delete(String domain, String type, Long id) {
        redisTemplate.delete(buildKey(domain, type, id));
    }

}
