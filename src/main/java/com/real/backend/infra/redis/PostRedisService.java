package com.real.backend.infra.redis;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOGGLE_LIKE_LUA = """
        local likeKey = KEYS[1]
        local countKey = KEYS[2]
        local noticeId = ARGV[1]

        if redis.call("SISMEMBER", likeKey, noticeId) == 1 then
            redis.call("SREM", likeKey, noticeId)
            redis.call("DECR", countKey)
            return 0
        else
            redis.call("SADD", likeKey, noticeId)
            redis.call("INCR", countKey)
            return 1
        end
    """;

    private String buildKey(String domain, String type, Long id) {
        return domain + ":" + type + ":" + id;  // ex) news:view:12
    }

    public void initCount(String domain, String type, Long id, Long value) {
        String key = buildKey(domain, type, id);
        redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    public Long increment(String domain, String type, Long id) {
        String key = buildKey(domain, type, id);
        return redisTemplate.opsForValue().increment(key);
    }

    public void decrement(String domain, String type, Long id) {
        redisTemplate.opsForValue().decrement(buildKey(domain, type, id));
    }

    public Long getCount(String domain, String type, Long id) {
        Object val = redisTemplate.opsForValue().get(buildKey(domain, type, id));
        return val == null ? 0L : Long.parseLong(val.toString());
    }

    public void delete(String domain, String type, Long id) {
        redisTemplate.delete(buildKey(domain, type, id));
    }

    public List<Long> getIds(String domain, String type) {
        String str = domain + ":" + type + ":*";

        Set<String> keys = redisTemplate.keys(str);
        return keys.stream()
            .map(k -> (k.substring((domain+":"+type+":").length())))
            .map(Long::parseLong)
            .collect(Collectors.toList());
    }

    public boolean userLiked(String domain, Long userId, Long noticeId) {
        String key = domain+":like:"+"user:"+userId;

        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, noticeId.toString()));
    }

    public boolean toggleLikeInRedis(String domain, Long userId, Long noticeId) {
        String likeKey = domain + ":like:user:" + userId;
        String countKey = domain + ":like:count:" + noticeId;

        RedisScript<Long> script = RedisScript.of(TOGGLE_LIKE_LUA, Long.class);

        Long result = redisTemplate.execute(
            script,
            List.of(likeKey, countKey),
            noticeId.toString()
        );

        return result != null && result == 1;
    }
}
