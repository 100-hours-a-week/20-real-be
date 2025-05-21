package com.real.backend.domain.news.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.news.component.NewsFinder;
import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.domain.NewsLike;
import com.real.backend.domain.news.dto.NewsLikeResponseDTO;
import com.real.backend.domain.news.repository.NewsLikeRepository;
import com.real.backend.domain.news.repository.NewsRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
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

        boolean liked = postRedisService.userLiked("news", userId, newsId);
        postRedisService.createUserLike("news", userId, newsId, liked);

        return NewsLikeResponseDTO.of(newsId, !liked);
    }
}
