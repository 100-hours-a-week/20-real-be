package com.real.backend.modules.news.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.news.component.NewsFinder;
import com.real.backend.modules.news.domain.News;
import com.real.backend.modules.news.dto.NewsLikeResponseDTO;
import com.real.backend.modules.user.component.UserFinder;
import com.real.backend.modules.user.domain.User;
import com.real.backend.infra.redis.PostRedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsLikeService {
    private final UserFinder userFinder;
    private final NewsFinder newsFinder;
    private final PostRedisService postRedisService;

    @Transactional
    public NewsLikeResponseDTO editNewsLike(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);
        User user = userFinder.getUser(userId);

        boolean liked = postRedisService.toggleLikeInRedis("news", userId, newsId);

        return NewsLikeResponseDTO.of(newsId, !liked);
    }
}
