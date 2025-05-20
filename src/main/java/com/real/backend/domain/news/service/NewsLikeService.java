package com.real.backend.domain.news.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.news.component.NewsFinder;
import com.real.backend.domain.news.domain.News;
import com.real.backend.domain.news.domain.NewsLike;
import com.real.backend.domain.news.dto.NewsLikeResponseDTO;
import com.real.backend.domain.news.repository.NewsLikeRepository;
import com.real.backend.domain.user.component.UserFinder;
import com.real.backend.domain.user.domain.User;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsLikeService {
    private final NewsLikeRepository newsLikeRepository;
    private final UserFinder userFinder;
    private final NewsFinder newsFinder;

    @Transactional
    public NewsLikeResponseDTO editNewsLike(Long newsId, Long userId) {
        News news = newsFinder.getNews(newsId);
        User user = userFinder.getUser(userId);

        newsLikeRepository.insertOrToggle(userId, newsId);
        NewsLike newsLike = newsLikeRepository.findByNewsAndUser(news, user).orElseThrow(() -> new NotFoundException("not found"));

        return NewsLikeResponseDTO.from(newsLike);
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
