package com.real.backend.modules.wiki.component;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.common.exception.NotFoundException;
import com.real.backend.modules.wiki.domain.Wiki;
import com.real.backend.modules.wiki.repository.WikiRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WikiFinder {

    private final WikiRepository wikiRepository;

    @Transactional(readOnly = true)
    public Wiki getWiki(Long wikiId) {
        Wiki wiki = wikiRepository.findById(wikiId).orElseThrow(() -> new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다."));
        if (wiki.getDeletedAt() != null) {
            throw new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다.");
        }
        return wiki;
    }
}
