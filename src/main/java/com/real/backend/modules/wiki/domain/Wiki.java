package com.real.backend.modules.wiki.domain;

import com.real.backend.common.base.BaseEntity;

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
public class Wiki extends BaseEntity {
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

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String editorName;

    public void updateYdoc(String ydoc) { this.ydoc = ydoc; }
    public void updateEditorName(String editorName) { this.editorName = editorName; }
    public void updateHtml(String html) { this.html = html; }
}
