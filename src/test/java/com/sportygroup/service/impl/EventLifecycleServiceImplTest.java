package com.sportygroup.service.impl;

import com.sportygroup.component.InMemoryEventStore;
import com.sportygroup.service.LiveEventScheduler;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Unit tests for EventLifecycleServiceImpl.
 *
 * Verifies that:
 * - store is updated
 * - scheduler is called to start/cancel per-event jobs
 */
class EventLifecycleServiceImplTest {

  @Test
  void markLiveShouldUpdateStoreAndSchedule() {
    InMemoryEventStore store = spy(new InMemoryEventStore());
    LiveEventScheduler scheduler = mock(LiveEventScheduler.class);

    EventLifecycleServiceImpl service = new EventLifecycleServiceImpl(store, scheduler);

    service.markLive("match1");

    verify(store).markLive("match1");
    verify(scheduler).ensureScheduled("match1");
  }

  @Test
  void markNotLiveShouldUpdateStoreAndCancelSchedule() {
    InMemoryEventStore store = spy(new InMemoryEventStore());
    LiveEventScheduler scheduler = mock(LiveEventScheduler.class);

    EventLifecycleServiceImpl service = new EventLifecycleServiceImpl(store, scheduler);

    service.markNotLive("match1");

    verify(store).markNotLive("match1");
    verify(scheduler).cancelIfScheduled("match1");
  }
}
