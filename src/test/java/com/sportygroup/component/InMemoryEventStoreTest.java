package com.sportygroup.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the in-memory event store.
 */
class InMemoryEventStoreTest {

  @Test
  void markLiveAndNotLiveShouldUpdateState() {
    InMemoryEventStore store = new InMemoryEventStore();

    assertFalse(store.isLive("e1"));
    store.markLive("e1");
    assertTrue(store.isLive("e1"));
    assertTrue(store.snapshotLiveEvents().contains("e1"));

    store.markNotLive("e1");
    assertFalse(store.isLive("e1"));
    assertFalse(store.snapshotLiveEvents().contains("e1"));
  }

  @Test
  void snapshotShouldBeUnmodifiable() {
    InMemoryEventStore store = new InMemoryEventStore();
    store.markLive("e1");

    assertThrows(UnsupportedOperationException.class, () -> store.snapshotLiveEvents().add("e2"));
  }
}
