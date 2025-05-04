package com.real.backend.domain.notice.domain;

import com.real.backend.domain.user.domain.User;
import com.real.backend.post.Post;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
    private String originalUrl;
    private String platform;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
