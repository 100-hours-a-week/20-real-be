package com.real.backend.infra.redis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiRedisService {
    private final WikiRepository wikiRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void updateWiki(Long wikiId, String ydoc, String html, String username) {
        LocalDateTime now = LocalDateTime.now();
        String title = wikiRepository.getWikiTitleById(wikiId);
        Map<String, String> wikiMap = new HashMap<>();
        wikiMap.put("title", title);
        wikiMap.put("html", html);
        wikiMap.put("editor_name", username);
        wikiMap.put("updated_at", now.toString());
        wikiMap.put("ydoc", ydoc);

        redisTemplate.opsForHash().putAll("wiki:" + wikiId, wikiMap);

        addZSetWiki(wikiId, now.toEpochSecond(ZoneOffset.UTC));
        addZSetWikiTitle(wikiId, title);
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
                .id(wikiId)
                .title(title)
                .ydoc(ydoc)
                .html(html)
                .editorName(editor)
                .updatedAt(LocalDateTime.parse(updatedAt))
                .build();
        }
    }

    public String getUpdatedAtByWikiId(Long wikiId) {
        Map<Object, Object> wikiMap = redisTemplate.opsForHash().entries("wiki:" + wikiId);
        return (String) wikiMap.get("updated_at");
    }

    public void loadAllWikisToRedis() {
        List<Wiki> wikis = wikiRepository.findAll();
        for (Wiki wiki : wikis) {
            String key = "wiki:" + wiki.getId();
            Map<String, String> hash = new HashMap<>();
            hash.put("html", wiki.getHtml());
            hash.put("editor_name", wiki.getEditorName());
            hash.put("updated_at", wiki.getUpdatedAt().toString());
            hash.put("ydoc", wiki.getYdoc());
            hash.put("title", wiki.getTitle());

            redisTemplate.opsForHash().putAll(key, hash);

            double score = wiki.getUpdatedAt().toEpochSecond(ZoneOffset.UTC);
            addZSetWiki(wiki.getId(), score);
            addZSetWikiTitle(wiki.getId(), wiki.getTitle());
        }
    }

    public void addZSetWiki(Long wikiId, double score) {
        redisTemplate.opsForZSet().add("wikis:sorted:latest", String.valueOf(wikiId), score);
    }

    public void addZSetWikiTitle(Long wikiId, String title) {
        redisTemplate.opsForZSet().add("wikis:title:index", title+":"+wikiId.toString(), 0);
    }
}
