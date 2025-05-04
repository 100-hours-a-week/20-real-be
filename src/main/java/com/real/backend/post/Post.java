package com.real.backend.post;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.real.backend.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
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

    public void increaseTotalViewCount() {this.totalViewCount++;}
    public void increaseLikesCount() {this.likeCount++;}
    public void decreaseLikesCount() {this.likeCount--;}
    public void increaseCommentCount() {this.commentCount++;}
    public void decreaseCommentCount() {this.commentCount--;}

}
