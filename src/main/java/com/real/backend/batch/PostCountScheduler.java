package com.real.backend.batch;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.news.repository.NewsRepository;
import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.infra.redis.PostRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostCountScheduler {

    private final PostRedisService postRedisService;
    private final NoticeRepository noticeRepository;
    private final NewsRepository newsRepository;

    @Transactional
    @Scheduled(cron = "*/3 * * * * *")
    public void syncNoticeCountsToDB() {
        List<Long> noticeIds = postRedisService.getIds("notice", "totalView");

        for (Long noticeId : noticeIds) {
            Long totalView = postRedisService.getCount("notice", "totalView", noticeId);
            Long likeCount = postRedisService.getCount("notice", "like", noticeId);
            Long commentCount = postRedisService.getCount("notice", "comment", noticeId);

            noticeRepository.updateCounts(noticeId, totalView, likeCount, commentCount);
        }
    }

    @Transactional
    @Scheduled(cron = "*/3 * * * * *")
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
    protected void resetTodayViewCount() {
        List<Long> newsIds = postRedisService.getIds("news", "todayView");
        for (Long newsId : newsIds) {
            postRedisService.delete("news", "todayView", newsId);
        }
        newsRepository.resetTodayViewCount();
    }
}
