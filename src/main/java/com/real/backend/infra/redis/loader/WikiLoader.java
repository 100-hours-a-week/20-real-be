package com.real.backend.infra.redis.loader;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.real.backend.infra.redis.WikiRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WikiLoader implements ApplicationRunner {
    private final WikiRedisService wikiRedisService;

    @Override
    public void run(ApplicationArguments args) {
        wikiRedisService.loadAllWikisToRedis();
    }
}
