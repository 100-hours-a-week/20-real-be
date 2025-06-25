package com.real.backend.infra.redis.loader;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.infra.redis.NoticeRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeReadCacheLoader implements ApplicationRunner {

    private final NoticeRedisService noticeRedisService;

    @Transactional(readOnly = true)
    @Override
    public void run(ApplicationArguments args) {
        noticeRedisService.loadAllUserNoticeRead();
    }
}
