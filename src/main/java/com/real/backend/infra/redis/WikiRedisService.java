package com.real.backend.infra.redis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiRedisService {
    private final WikiRepository wikiRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final String latestZSetKey = "wikis:sorted:latest";
    private final String titleZSetKey = "wikis:sorted:title";


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

    public void loadAllWikiDataToRedis() {
        List<Wiki> wikis = wikiRepository.findAllWithoutDeleted();
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        Set<String> titleSet = getZSetValues(titleZSetKey);
        Set<String> latestSet = getZSetValues(latestZSetKey);

        for (Wiki wiki : wikis) {
            String titleEntry = wiki.getTitle() + ":" + wiki.getId();
            String latestEntry = String.valueOf(wiki.getId());
            if (!titleSet.contains(titleEntry)) {
                zSetOps.add(titleZSetKey, titleEntry, 0);
            }
            if (!latestSet.contains(latestEntry)) {
                long score = wiki.getUpdatedAt().toEpochSecond(ZoneOffset.UTC);
                zSetOps.add(latestZSetKey, latestEntry, score);
            }
        }
    }

    private Set<String> getZSetValues(String key) {

        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> existing = zSetOps.range(key, 0, -1);

        return (existing != null) ? existing : Set.of();
    }

    public void addZSetWiki(Long wikiId, double score) {
        redisTemplate.opsForZSet().add(latestZSetKey, String.valueOf(wikiId), score);
    }

    public void addZSetWikiTitle(Long wikiId, String title) {
        redisTemplate.opsForZSet().add(titleZSetKey, title+":"+wikiId.toString(), 0);
    }
}
