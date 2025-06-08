package com.real.backend.modules.news.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.news.domain.News;
import com.real.backend.modules.news.repository.NewsRepository;
import com.real.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsFinder {
    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public News getNews(Long newsId) {
        News news = newsRepository.findById(newsId).orElseThrow(() -> new NotFoundException("해당 id를 가진 뉴스가 존재하지 않습니다."));
        if (news.getDeletedAt() != null) {
            throw new NotFoundException("해당 id를 가진 뉴스가 존재하지 않습니다.");
        }
        return news;
    }
}
