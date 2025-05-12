package com.real.backend.domain.news.domain;

import java.util.List;

import com.real.backend.post.Post;

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

    public void increaseTodayViewCount() {this.todayViewCount++;}

}
