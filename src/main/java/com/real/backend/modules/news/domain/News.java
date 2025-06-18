package com.real.backend.modules.news.domain;

import java.util.List;

import com.real.backend.infra.ai.dto.NewsAiResponseDTO;
import com.real.backend.common.base.Post;
import com.real.backend.infra.ai.dto.WikiNewsAiResponseDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class News extends Post {
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long todayViewCount;

    private String imageUrl;

    // TODO cascade 전략 수정하기
    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsComment> newsComments;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsLike> newsLikes;

    public static News of(NewsAiResponseDTO newsAiResponseDTO, String content, String url) {
        return News.builder()
            .title(newsAiResponseDTO.headline())
            .content(content)
            .tag("뉴스")
            .todayViewCount(0L)
            .totalViewCount(0L)
            .imageUrl(url)
            .summary(newsAiResponseDTO.summary())
            .likeCount(0L)
            .commentCount(0L)
            .build();
    }

    public static News of(WikiNewsAiResponseDTO wikiNewsAiResponseDTO, String imageUrl) {
        return News.builder()
            .title(wikiNewsAiResponseDTO.getHeadline())
            .content(wikiNewsAiResponseDTO.getNews())
            .tag("뉴스")
            .todayViewCount(0L)
            .totalViewCount(0L)
            .imageUrl(imageUrl)
            .summary(wikiNewsAiResponseDTO.getSummary())
            .likeCount(0L)
            .commentCount(0L)
            .build();
    }
}
