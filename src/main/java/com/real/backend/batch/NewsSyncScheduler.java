package com.real.backend.batch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.real.backend.infra.redis.NewsRedisService;
import com.real.backend.infra.redis.PostRedisService;
import com.real.backend.modules.news.repository.NewsRepository;
import com.real.backend.modules.news.service.NewsAiService;
import com.real.backend.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsSyncScheduler {

    private final PostRedisService postRedisService;
    private final NewsRedisService newsRedisService;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsAiService newsAiService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    @Scheduled(cron = "0 0 */3 * * *")
    // @Scheduled(cron = "*/1 * * * * *")
    public void syncNewsCountsToDB() {
        List<Long> newsIds = postRedisService.getIds("news", "totalView");

        for (Long newsId : newsIds) {
            Long totalView = postRedisService.getCount("news", "totalView", newsId);
            Long todayView = postRedisService.getCount("news", "todayView", newsId);
            Long likeCount = postRedisService.getCount("news", "like", newsId);
            Long commentCount = postRedisService.getCount("news", "comment", newsId);

            newsRepository.updateCounts(newsId, totalView, todayView, likeCount, commentCount);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void resetNewsTodayViewCount() {
        List<Long> newsIds = postRedisService.getIds("news", "todayView");
        for (Long newsId : newsIds) {
            postRedisService.delete("news", "todayView", newsId);
        }
        newsRepository.resetTodayViewCount();
    }

    @Transactional
    @Scheduled(cron = "0 0 */3 * * *")
    // @Scheduled(cron = "*/1 * * * * *")
    public void SyncNewsLikeToDB(){
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(181); // 최근 3시간 1분 로그인한 유저
        List<Long> activeUserIds = userRepository.findRecentlyActiveUserIds(threshold);

        newsRedisService.syncLike(activeUserIds);
    }

    @Transactional
    @Scheduled(cron = "0 10 12 * * *")
    public void createNewsAi() throws JsonProcessingException {
        String lockKey = "batch:news:lock";
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofMinutes(10));

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                newsAiService.createNewsAiByRandomWiki();
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
