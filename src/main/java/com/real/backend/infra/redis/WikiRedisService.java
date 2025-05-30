package com.real.backend.infra.redis;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiRedisService {
    private final WikiRepository wikiRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void updateWiki(Long wikiId, String ydoc, String html, String username) {
        Map<String, String> wikiMap = new HashMap<>();
        wikiMap.put("html", html);
        wikiMap.put("editor_name", username);
        wikiMap.put("updated_at", LocalDateTime.now().toString());
        wikiMap.put("ydoc", ydoc);

        redisTemplate.opsForHash().putAll("wiki:" + wikiId, wikiMap);
    }

    @Transactional
    public void flushToDB(Long wikiId) {
        Map<Object, Object> wikiMap = redisTemplate.opsForHash().entries("wiki:" + wikiId);
        String html = (String) wikiMap.get("html");
        String editor = (String) wikiMap.get("editor_name");
        String updatedAt = (String) wikiMap.get("updated_at");
        String ydoc = (String) wikiMap.get("ydoc");

        Wiki wiki = wikiRepository.findById(wikiId).orElse(new Wiki());
        wiki.updateHtml(html);
        wiki.updateYdoc(ydoc);
        wiki.updateEditorName(editor);
        wiki.updateUpdatedAt(updatedAt);

        wikiRepository.save(wiki);

        redisTemplate.delete("wiki:" + wikiId);
    }

    @Transactional
    public void deleteWikiById(Long wikiId) {
        redisTemplate.delete("wiki:" + wikiId);
    }

    @Transactional(readOnly = true)
    public Wiki getWikiById(String title) {
        Long wikiId = wikiRepository.getWikiIdByTitle(title);
        Map<Object, Object> wikiMap = redisTemplate.opsForHash().entries("wiki:" + wikiId);
        String html = (String) wikiMap.get("html");
        String editor = (String) wikiMap.get("editor_name");
        String updatedAt = (String) wikiMap.get("updated_at");
        String ydoc = (String) wikiMap.get("ydoc");

        if (ydoc == null || html == null || editor == null || updatedAt == null) {
            return null;
        } else {
            return Wiki.builder()
                .title(title)
                .ydoc(ydoc)
                .html(html)
                .editorName(editor)
                .updatedAt(LocalDateTime.parse(updatedAt))
                .build();
        }
    }
}
