package com.real.backend.common.base;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false)
    private String tag;
    @Column(nullable = true)
    private String summary;
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long likeCount;
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long totalViewCount;
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private Long commentCount;

    public void updatePost(String title, String content, String tag) {
        this.title = title;
        this.content = content;
        this.tag = tag;
    }
}
