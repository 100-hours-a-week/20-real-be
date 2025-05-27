package com.real.backend.batch;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.notice.repository.NoticeRepository;
import com.real.backend.domain.user.repository.UserRepository;
import com.real.backend.infra.redis.NoticeRedisService;
import com.real.backend.infra.redis.PostRedisService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeSyncScheduler {

    private final PostRedisService postRedisService;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final NoticeRedisService noticeRedisService;

    @Transactional
    @Scheduled(cron = "0 0 */3 * * *")
    // @Scheduled(cron = "*/1 * * * * *")
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
    @Scheduled(cron = "0 0 */3 * * *")
    // @Scheduled(cron = "* 14 15 * * *")
    public void SyncNoticeLikeToDB(){
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(181); // 최근 3시간 1분 로그인한 유저
        List<Long> activeUserIds = userRepository.findRecentlyActiveUserIds(threshold);

        noticeRedisService.syncLike(activeUserIds);
    }

    @Transactional
    @Scheduled(cron = "0 0 */3 * * *")
    // @Scheduled(cron = "* 42 17 * * *")
    public void syncNoticeReadToDB() {
        noticeRedisService.syncNoticeRead();
    }

}
