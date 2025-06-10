package com.real.backend.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.real.backend.modules.wiki.service.WikiSyncService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WikiSyncScheduler {
    private final WikiSyncService wikiSyncService;

    @Transactional
    @Scheduled(cron = "0 */30 * * * *")
    public void syncWiki() {
        wikiSyncService.syncWiki();
    }
}
