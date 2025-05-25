package com.real.backend.domain.wiki.domain;

import com.real.backend.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
public class Wiki extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(27)")
    private String title;

    @Lob
    private byte[] content;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String editorName;

    public void updateContent(byte[] content) { this.content = content; }
    public void updateEditorName(String editorName) { this.editorName = editorName; }
}
