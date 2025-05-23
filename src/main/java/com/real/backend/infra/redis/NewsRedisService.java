package com.real.backend.infra.redis;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.real.backend.domain.news.component.NewsFinder;
import com.real.backend.domain.news.domain.NewsLike;
import com.real.backend.domain.news.repository.NewsLikeRepository;
import com.real.backend.domain.user.component.UserFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NewsLikeRepository newsLikeRepository;
    private final UserFinder userFinder;
    private final NewsFinder newsFinder;

    public void syncLike(List<Long> userIds) {

        for (Long userId : userIds) {
            String likeKey = "news:like:user:"+userId;
            String cancelLikeKey = "news:like:cancel:user:"+userId;

            Set<Object> likedIds = redisTemplate.opsForSet().members(likeKey);
            Set<Object> canceledIds = redisTemplate.opsForSet().members(cancelLikeKey);

            // 1. 좋아요 상태 반영 (true)
            if (likedIds != null) {
                for (Object newsIdObj : likedIds) {
                    Long newsId = Long.valueOf((String) newsIdObj);

                    newsLikeRepository.findByUserIdAndNewsId(userId, newsId)
                        .ifPresentOrElse(
                            like -> {
                                if (!like.getIsActivated()) {
                                    newsLikeRepository.updateIsActivated(userId, newsId, true);
                                }
                            },
                            () -> {
                                NewsLike newLike = NewsLike.builder()
                                    .user(userFinder.getUser(userId))
                                    .news(newsFinder.getNews(newsId))
                                    .isActivated(true)
                                    .build();
                                newsLikeRepository.save(newLike);
                            }
                        );
                }
            }

            // 2. 좋아요 취소 상태 반영 (false)
            if (canceledIds != null) {
                for (Object newsIdObj : canceledIds) {
                    Long newsId = Long.valueOf((String) newsIdObj);

                    // 좋아요 Set에도 있으면 무시해야 함
                    if (likedIds != null && likedIds.contains(newsIdObj)) continue;

                    newsLikeRepository.findByUserIdAndNewsId(userId, newsId)
                        .ifPresent(like -> {
                            if (like.getIsActivated()) {
                                newsLikeRepository.updateIsActivated(userId, newsId, false);
                            }
                        });

                    // 처리 완료되면 Redis에서 삭제
                    redisTemplate.opsForSet().remove(cancelLikeKey, newsIdObj);
                }
            }
        }
    }
}
