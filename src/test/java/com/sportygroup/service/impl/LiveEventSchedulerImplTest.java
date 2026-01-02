package com.sportygroup.service.impl;

import com.sportygroup.client.ExternalScoreClient;
import com.sportygroup.model.ExternalScoreResponse;
import com.sportygroup.publish.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LiveEventSchedulerImpl.
 *
 * These tests do NOT require Spring; they validate scheduling & publishing behavior via mocks.
 */
class LiveEventSchedulerImplTest {

  private static void setField(Object target, String name, Object value) {
    try {
      var f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void ensureScheduledShouldScheduleOnlyOncePerEvent() {
    TaskScheduler scheduler = mock(TaskScheduler.class);
    ExternalScoreClient external = mock(ExternalScoreClient.class);
    MessagePublisher publisher = mock(MessagePublisher.class);

    // capture runnable passed to scheduleAtFixedRate
    AtomicReference<Runnable> captured = new AtomicReference<>();
    ScheduledFuture<?> future = mock(ScheduledFuture.class);

    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong()))
        .thenAnswer(inv -> {
          captured.set(inv.getArgument(0));
          return future;
        });

    RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(1).fixedBackoff(0).build();

    LiveEventSchedulerImpl impl = new LiveEventSchedulerImpl(scheduler, external, publisher, retryTemplate);

    // set @Value-injected field for unit test
    setField(impl, "pollingIntervalMs", 1000L);

    impl.ensureScheduled("e1");
    impl.ensureScheduled("e1"); // second call should NOT schedule again

    verify(scheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), eq(1000L));
    assertNotNull(captured.get(), "Runnable should be captured from scheduler");

    // When runnable runs, it should call external and publish
    ExternalScoreResponse resp = new ExternalScoreResponse();
    resp.setEventId("e1");
    resp.setCurrentScore("0:0");
    when(external.fetchScore("e1")).thenReturn(resp);

    captured.get().run();

    verify(external, times(1)).fetchScore("e1");
    verify(publisher, times(1)).publish(any());
  }

/*  @Test
  void cancelIfScheduledShouldCancelFuture() {
    TaskScheduler scheduler = mock(TaskScheduler.class);
    ExternalScoreClient external = mock(ExternalScoreClient.class);
    MessagePublisher publisher = mock(MessagePublisher.class);
    RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(1).fixedBackoff(0).build();

    ScheduledFuture<?> future = mock(ScheduledFuture.class);
    when(scheduler.scheduleAtFixedRate(any(Runnable.class), anyLong())).thenReturn(future);

    LiveEventSchedulerImpl impl = new LiveEventSchedulerImpl(scheduler, external, publisher, retryTemplate);

    // set @Value-injected field for unit test
    setField(impl, "pollingIntervalMs", 1000L);

    impl.ensureScheduled("e1");
    impl.cancelIfScheduled("e1");

    verify(future).cancel(false);
  }*/
}
