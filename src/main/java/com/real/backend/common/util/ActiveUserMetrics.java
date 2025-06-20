package com.real.backend.common.util;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Objects;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Component
public class ActiveUserMetrics {

    private final RedisTemplate<String, String> redisTemplate;
    private final MeterRegistry meterRegistry;

    public ActiveUserMetrics(RedisTemplate<String, String> redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;

        Gauge.builder("active_user_counts", this, ActiveUserMetrics::countActiveUsers)
            .description("Active User Count")
            .register(meterRegistry);
    }

    public int countActiveUsers() {
        int count = 0;
        Cursor<byte[]> cursor = Objects.requireNonNull(
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection()
                .scan(ScanOptions.scanOptions().match("user:active:*").count(1000).build())
        );

        while (cursor.hasNext()) {
            cursor.next();
            count++;
        }

        return count;
    }
}
