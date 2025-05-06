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
import com.real.backend.domain.user.service.UserService;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsLikeService {
    private final NewsLikeRepository newsLikeRepository;
    private final NewsRepository newsRepository;
    private final UserFinder userFinder;
    private final NewsFinder newsFinder;

    @Transactional
    public NewsLikeResponseDTO editNewsLike(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);
        User user = userFinder.getUser(userId);
        NewsLike newsLike = newsLikeRepository.findByNewsAndUser(news, user).orElse(null);

        if (newsLike == null) {
            newsLike = NewsLike.builder()
                .isActivated(false)
                .user(user)
                .news(news)
                .build();
        }

        newsLike.updateIsActivated();

        if (newsLike.getIsActivated()) news.increaseLikesCount();
        else news.decreaseLikesCount();

        newsRepository.save(news);
        return NewsLikeResponseDTO.from(newsLikeRepository.save(newsLike));

    }

    @Transactional(readOnly = true)
    public boolean userIsLiked(Long newsId, Long userId) {
        NewsLike newsLike = getNewsLike(newsId, userId);
        if (newsLike == null) return false;
        return newsLike.getIsActivated();

    }

    @Transactional(readOnly = true)
    public NewsLike getNewsLike(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);
        User user = userFinder.getUser(userId);

        return newsLikeRepository.findByNewsAndUser(news, user).orElse(null);
    }

}
