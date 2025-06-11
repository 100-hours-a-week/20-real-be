package com.real.backend.infra.redis.loader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.user.domain.UserNoticeRead;
import com.real.backend.modules.user.repository.UserNoticeReadRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeReadCacheLoader implements ApplicationRunner {

    private final UserNoticeReadRepository userNoticeReadRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    @Override
    public void run(ApplicationArguments args) {
        List<UserNoticeRead> allReads = userNoticeReadRepository.findAll();

        Map<Long, List<String>> readsByUser = allReads.stream()
            .collect(Collectors.groupingBy(
                r -> r.getUser().getId(),
                Collectors.mapping(r -> String.valueOf(r.getNotice().getId()), Collectors.toList())
            ));

        readsByUser.forEach((userId, noticeIds) -> {
            String key = "notice:read:user:" + userId;
            redisTemplate.opsForSet().add(
                key,
                noticeIds.toArray(new String[0])
            );
        });
    }
}
