package com.real.backend.domain.wiki.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.real.backend.domain.wiki.domain.SearchMethod;
import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.domain.wiki.repository.WikiRepository;
import com.real.backend.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WikiService {

    private final WikiRepository wikiRepository;

    public void createWiki(WikiCreateRequestDTO wikiCreateRequestDTO, String userName) {
        wikiRepository.save(Wiki.builder()
            .title(wikiCreateRequestDTO.getTitle())
            .editorName(userName)
            .content(null)
            .build());
    }

    public Wiki getWiki(String title, SearchMethod method) {
        if (SearchMethod.NORMAL.equals(method)) {
            return wikiRepository.findByTitle(title).orElseThrow(() -> new NotFoundException("해당 제목을 가진 위키가 존재하지 않습니다."));
        } else {
            List<Long> wikiIds = wikiRepository.getAllId();
            Long randomId = wikiIds.get((int) (Math.random() * wikiIds.size()));
            return wikiRepository.findById(randomId).orElseThrow(() -> new NotFoundException("해당 id를 가진 위키가 존재하지 않습니다."));
        }
    }
}
