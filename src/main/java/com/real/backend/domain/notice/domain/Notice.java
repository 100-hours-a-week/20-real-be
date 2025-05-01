package com.real.backend.domain.notice.domain;

import com.real.backend.post.Post;

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
public class Notice extends Post {
    private String summation;
    private String originalUrl;
}
