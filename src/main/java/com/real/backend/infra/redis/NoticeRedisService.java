package com.real.backend.infra.redis;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.real.backend.domain.notice.component.NoticeFinder;
import com.real.backend.domain.notice.domain.NoticeLike;
import com.real.backend.domain.notice.repository.NoticeLikeRepository;
import com.real.backend.domain.user.component.UserFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final NoticeLikeRepository noticeLikeRepository;
    private final UserFinder userFinder;
    private final NoticeFinder noticeFinder;

    public void createUserNoticeRead(Long userId, Long noticeId) {
        String key = "notice:read:user"+userId;
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForSet().add(key, noticeId.toString());
        }
    }

    public void syncLike(List<Long> userIds) {

        for (Long userId : userIds) {
            String likeKey = "notice:like:user:"+userId;
            String cancelLikeKey = "notice:like:cancel:user:"+userId;

            Set<Object> likedIds = redisTemplate.opsForSet().members(likeKey);
            Set<Object> canceledIds = redisTemplate.opsForSet().members(cancelLikeKey);

            // 1. 좋아요 상태 반영 (true)
            if (likedIds != null) {
                for (Object noticeIdObj : likedIds) {
                    Long noticeId = Long.valueOf((String) noticeIdObj);

                    noticeLikeRepository.findByUserIdAndNoticeId(userId, noticeId)
                        .ifPresentOrElse(
                            like -> {
                                if (!like.getIsActivated()) {
                                    noticeLikeRepository.updateIsActivated(userId, noticeId, true);
                                }
                            },
                            () -> {
                                NoticeLike newLike = NoticeLike.builder()
                                    .user(userFinder.getUser(userId))
                                    .notice(noticeFinder.getNotice(noticeId))
                                    .isActivated(true)
                                    .build();
                                noticeLikeRepository.save(newLike);
                            }
                        );
                }
            }

            // 2. 좋아요 취소 상태 반영 (false)
            if (canceledIds != null) {
                for (Object noticeIdObj : canceledIds) {
                    Long noticeId = Long.valueOf((String) noticeIdObj);

                    // 좋아요 Set에도 있으면 무시해야 함
                    if (likedIds != null && likedIds.contains(noticeIdObj)) continue;

                    noticeLikeRepository.findByUserIdAndNoticeId(userId, noticeId)
                        .ifPresent(like -> {
                            if (like.getIsActivated()) {
                                noticeLikeRepository.updateIsActivated(userId, noticeId, false);
                            }
                        });

                    // 처리 완료되면 Redis에서 삭제
                    redisTemplate.opsForSet().remove(cancelLikeKey, noticeIdObj);
                }
            }
        }
    }
}
