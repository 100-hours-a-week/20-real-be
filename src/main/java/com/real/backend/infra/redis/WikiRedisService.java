package com.real.backend.infra.redis;

import java.time.LocalDateTime;
import java.util.Arrays;

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
    private final RedisTemplate<String, byte[]> redisTemplateByteArray;

    public void updateWiki(Long wikiId, byte[] content, String html, String username) {
        redisTemplate.opsForValue().set("wiki:html:"+wikiId, html);
        redisTemplate.opsForValue().set("wiki:editor_name:"+wikiId, username);
        redisTemplate.opsForValue().set("wiki:updated_at:"+wikiId, LocalDateTime.now().toString());
        redisTemplateByteArray.opsForValue().set("wiki:content:"+wikiId, content);
    }

    @Transactional
    public void flushToDB(Long wikiId) {
        byte[] content = redisTemplateByteArray.opsForValue().get("wiki:content:" + wikiId);
        String html = (String)redisTemplate.opsForValue().get("wiki:html:" + wikiId);
        String editor = (String)redisTemplate.opsForValue().get("wiki:editor_name:" + wikiId);
        String updatedAt = (String)redisTemplate.opsForValue().get("wiki:updated_at:" + wikiId);


        Wiki wiki = wikiRepository.findById(wikiId).orElse(new Wiki());
        wiki.updateHtml(html);
        wiki.updateYdoc(content);
        wiki.updateEditorName(editor);
        wiki.updateUpdatedAt(updatedAt);

        wikiRepository.save(wiki);

        redisTemplate.delete(Arrays.asList(
            "wiki:html:" + wikiId,
            "wiki:ydoc:" + wikiId,
            "wiki:editor_name:" + wikiId,
            "wiki:updated_at:" + wikiId
        ));
    }
}
