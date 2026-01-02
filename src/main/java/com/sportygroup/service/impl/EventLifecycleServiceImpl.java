package com.sportygroup.service.impl;

import com.sportygroup.component.InMemoryEventStore;
import com.sportygroup.service.EventLifecycleService;
import com.sportygroup.service.LiveEventScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Handles transitions between LIVE and NOT_LIVE.
 * <p>
 * Responsibilities:
 * - update in-memory state
 * - schedule/cancel per-event polling jobs
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class EventLifecycleServiceImpl implements EventLifecycleService {

    private final InMemoryEventStore store;
    private final LiveEventScheduler scheduler;

    public void markLive(String eventId) {
        store.markLive(eventId);
        scheduler.ensureScheduled(eventId);
        log.info("Event marked LIVE: {}", eventId);
    }

    public void markNotLive(String eventId) {
        store.markNotLive(eventId);
        scheduler.cancelIfScheduled(eventId);
        log.info("Event marked NOT_LIVE: {}", eventId);
    }
}
