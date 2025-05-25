package com.real.backend.batch;

import java.util.Arrays;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WikiSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, byte[]> redisTemplateByteArray;
    private final WikiRepository wikiRepository;

    @Transactional
    @Scheduled(fixedDelay = 180000) // 3분
    public void syncWiki() {
        // Redis에서 키 목록 가져오기
        Set<String> keys = redisTemplate.keys("wiki:content:*");
        if (keys == null || keys.isEmpty()) return;

        for (String contentKey : keys) {
            try {
                // wikiId 추출
                Long wikiId = Long.parseLong(contentKey.substring(("wiki:content:").length()));

                // Redis에서 값 가져오기
                byte[] content = redisTemplateByteArray.opsForValue().get("wiki:content:" + wikiId);
                String editor = (String)redisTemplate.opsForValue().get("wiki:editor_name:" + wikiId);
                String updatedAt = (String)redisTemplate.opsForValue().get("wiki:updated_at:" + wikiId);

                if (content == null || updatedAt == null)
                    continue;

                // DB에 저장 또는 갱신
                Wiki wiki = wikiRepository.findById(wikiId)
                    .orElse(new Wiki());
                wiki.updateContent(content);
                wiki.updateEditorName(editor);
                wiki.updateUpdatedAt(updatedAt);

                wikiRepository.save(wiki);

                // Redis 키 삭제 (선택 사항)
                redisTemplate.delete(Arrays.asList(
                    "wiki:content:" + wikiId,
                    "wiki:editor_name:" + wikiId,
                    "wiki:updated_at:" + wikiId
                ));
            } catch (Exception e) {
                // 예외 로깅
                System.err.println("Flush 실패: " + contentKey + " → " + e.getMessage());
            }
        }
    }
}
