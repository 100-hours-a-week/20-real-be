package com.real.backend.modules.wiki.dto;

public record WikiTitleCursor(String title, Long id) {
    public String getTitle() {
        return title;
    }

    public Long getId() {
        return id;
    }
}
