package com.real.backend.modules.user.service;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class UserServiceTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}
