package com.real.backend.domain.news.domain;

import java.util.List;

import com.real.backend.post.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class News extends Post {
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long todayViewCount;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long commentCount;

    private String imageUrl;

    @OneToMany
    private List<NewsComment> newsComments;

    public void increaseTodayViewCount() {this.todayViewCount++;}
    public void increaseCommentCount() {this.commentCount++;}
    public void decreaseCommentCount() {this.commentCount--;}
}
