package com.real.backend.modules.wiki.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class Wiki {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(27)")
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String ydoc;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String html;

    @Column(nullable = false)
    private String editorName;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
    public void updateUpdatedAt(String updatedAt) {
        this.updatedAt = LocalDateTime.parse(updatedAt);
    }

    public void updateYdoc(String ydoc) { this.ydoc = ydoc; }
    public void updateEditorName(String editorName) { this.editorName = editorName; }
    public void updateHtml(String html) { this.html = html; }
}
