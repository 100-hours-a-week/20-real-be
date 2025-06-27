package com.real.backend.modules.wiki.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.BadRequestException;
import com.real.backend.common.exception.NotFoundException;
import com.real.backend.infra.redis.WikiRedisService;
import com.real.backend.modules.wiki.component.WikiFinder;
import com.real.backend.modules.wiki.domain.SearchMethod;
import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.modules.wiki.dto.WikiResponseDTO;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiRepository wikiRepository;
    private final WikiRedisService wikiRedisService;
    private final WikiFinder wikiFinder;

    @Transactional
    public Wiki createWiki(WikiCreateRequestDTO wikiCreateRequestDTO, String userName) {
        return wikiRepository.save(Wiki.builder()
            .title(wikiCreateRequestDTO.getTitle())
            .editorName(userName)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .ydoc(null)
            .build());
    }

    @Transactional(readOnly = true)
    public Wiki getWiki(String title, SearchMethod method) {
        if (Objects.equals(title, " ") || Objects.equals(title, "")) {
            throw new BadRequestException("빈 문자열은 입력으로 들어올 수 없습니다.");
        }
        if (SearchMethod.NORMAL.equals(method)) {
            Wiki wiki = wikiRedisService.getWikiByTitle(title);
            if (wiki == null) {
                return wikiRepository.findByTitle(title).orElseThrow(() -> new NotFoundException("해당 제목을 가진 위키가 존재하지 않습니다."));
            }
            return wiki;
        } else {
            return getRandomWiki(wikiRepository.getAllId());
        }
    }

    @Transactional(readOnly = true)
    public WikiResponseDTO getWikiById(Long wikiId) {
        Wiki wiki = wikiRedisService.getWikiById(wikiId);
        if (wiki == null) {
            return WikiResponseDTO.from(wikiFinder.getWiki(wikiId));
        }
        return WikiResponseDTO.from(wiki);
    }

    @Transactional(readOnly = true)
    public Wiki getRandomWiki(List<Long> wikiIds) {
        Long randomId = wikiIds.get((int) (Math.random() * wikiIds.size()));
        return wikiFinder.getWiki(randomId);
    }

    @Transactional
    public void deleteWiki(Long wikiId) {
        Wiki wiki = wikiFinder.getWiki(wikiId);
        wiki.delete();
        wikiRedisService.deleteZSetWikiTitle(wikiId, wiki.getTitle());
        wikiRedisService.deleteZSetWikiUpdatedAt(wikiId);
        wikiRedisService.deleteWikiHash(wikiId);
        wikiRepository.save(wiki);
    }
}
