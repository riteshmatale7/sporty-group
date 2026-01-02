package com.sportygroup.component;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory state store.
 * <p>
 * Thread-safe and good enough for the assignment.
 * In a real system, this would likely be Redis/DB + leader election.
 */
@Component
public class InMemoryEventStore {

    private final Set<String> liveEvents = ConcurrentHashMap.newKeySet();

    public void markLive(String eventId) {
        liveEvents.add(eventId);
    }

    public void markNotLive(String eventId) {
        liveEvents.remove(eventId);
    }

    public boolean isLive(String eventId) {
        return liveEvents.contains(eventId);
    }

    public Set<String> snapshotLiveEvents() {
        return Collections.unmodifiableSet(liveEvents);
    }
}
