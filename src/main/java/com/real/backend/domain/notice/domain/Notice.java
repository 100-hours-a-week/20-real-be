package com.real.backend.domain.notice.domain;

import com.real.backend.domain.user.domain.User;
import com.real.backend.post.Post;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class Notice extends Post {
    private String originalUrl;
    private String platform;
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
