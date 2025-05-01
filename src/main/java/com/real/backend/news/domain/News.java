package com.real.backend.news.domain;

import com.real.backend.post.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    public void increaseTodayViewCount() {this.todayViewCount++;}
}
