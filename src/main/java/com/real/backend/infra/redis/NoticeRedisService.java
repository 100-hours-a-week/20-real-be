package com.real.backend.infra.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.real.backend.modules.notice.component.NoticeFinder;
import com.real.backend.modules.notice.domain.NoticeLike;
import com.real.backend.modules.notice.repository.NoticeLikeRepository;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.UserNoticeRead;
import com.real.backend.modules.user.repository.UserNoticeReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final NoticeLikeRepository noticeLikeRepository;
    private final UserFinder userFinder;
    private final NoticeFinder noticeFinder;
    private final UserNoticeReadRepository userNoticeReadRepository;

    public void createUserNoticeRead(Long userId, Long noticeId) {
        String key = "notice:read:user:"+userId;
        redisTemplate.opsForSet().add(key, noticeId.toString());
    }

    public Boolean getUserRead(Long userId, Long noticeId) {
        String key = "notice:read:user:"+userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, noticeId.toString()));
    }

    public List<Long> getUserReadList(Long userId) {
        String key = "notice:read:user:"+userId;
        Set<Object> noticeIds = redisTemplate.opsForSet().members(key);
        if (noticeIds == null) {
            return new ArrayList<>();
        }
        return noticeIds
            .stream()
            .map(noticeId -> Long.parseLong(noticeId.toString()))
            .toList();
    }

    public void userNoticeReadAll(List<Long> noticeIds, Long userId) {
        String redisKey = "notice:read:user:" + userId;
        redisTemplate.opsForSet().add(redisKey,
            noticeIds.stream().map(String::valueOf).toArray(String[]::new));
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

    public void syncNoticeRead() {
        String str = "notice:read:user:*";

        Set<String> keys = redisTemplate.keys(str);
        List<Long> ids = keys.stream()
            .map(k -> (k.substring(("notice:read:user:").length())))
            .map(Long::parseLong)
            .toList();

        for (Long userId : ids) {

            Set<Object> noticeIds = redisTemplate.opsForSet().members("notice:read:user:"+userId);

            if (noticeIds != null) {
                for (Object noticeIdObj : noticeIds) {
                    Long noticeId = Long.valueOf((String) noticeIdObj);
                    if (!userNoticeReadRepository.existsByUserIdAndNoticeId(userId, noticeId)) {
                        userNoticeReadRepository.save(
                            UserNoticeRead.builder()
                                .user(userFinder.getUser(userId))
                                .notice(noticeFinder.getNotice(noticeId))
                                .build());
                    };
                }
            }
        }
    }
}
