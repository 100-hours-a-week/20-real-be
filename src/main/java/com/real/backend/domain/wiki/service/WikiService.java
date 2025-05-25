package com.real.backend.domain.wiki.service;

import org.springframework.stereotype.Service;

import com.real.backend.domain.wiki.domain.Wiki;
import com.real.backend.domain.wiki.dto.WikiCreateRequestDTO;
import com.real.backend.domain.wiki.repository.WikiRepository;

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
}
