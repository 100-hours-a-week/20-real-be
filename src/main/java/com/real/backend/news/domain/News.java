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
    private long todayViewCount;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private long commentCount;

    private String imageUrl;
}
