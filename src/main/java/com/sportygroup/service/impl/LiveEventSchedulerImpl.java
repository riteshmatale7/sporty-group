package com.sportygroup.service.impl;

import com.sportygroup.client.ExternalScoreClient;
import com.sportygroup.model.LiveEventMessage;
import com.sportygroup.model.ExternalScoreResponse;
import com.sportygroup.publish.MessagePublisher;
import com.sportygroup.service.LiveEventScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Owns per-event scheduling.
 * <p>
 * Key design goal: when an event becomes LIVE, we start exactly one poller for it.
 * When it becomes NOT_LIVE, we cancel it.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class LiveEventSchedulerImpl implements LiveEventScheduler {

    private final TaskScheduler scheduler;
    private final ExternalScoreClient externalScoreClient;
    private final MessagePublisher publisher;
    private final RetryTemplate retryTemplate;
    @Value("${app.polling-interval-ms:10000}")
    private long pollingIntervalMs;

    /**
     * eventId -> scheduled future
     */
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();


    /**
     * Ensure there is a job scheduled for the given event.
     * Idempotent: calling this repeatedly will not schedule duplicates.
     */
    @Override
    public void ensureScheduled(String eventId) {
        jobs.computeIfAbsent(eventId, id -> {
            log.info("Scheduling poller for eventId={} every {}ms", id, pollingIntervalMs);
            return scheduler.scheduleAtFixedRate(() -> runOnce(id), pollingIntervalMs);
        });
    }

    /**
     * Cancel the job for this event if it exists.
     */
    @Override
    public void cancelIfScheduled(String eventId) {
        ScheduledFuture<?> future = jobs.remove(eventId);
        if (future != null) {
            log.info("Cancelling poller for eventId={}", eventId);
            future.cancel(false);
        }
    }

    /**
     * Single execution of a scheduled job.
     */
    private void runOnce(String eventId) {
        try {
            // Retry transient HTTP errors (timeouts, 5xx, etc.)
            ExternalScoreResponse score = retryTemplate.execute(ctx -> {
                try {
                    return externalScoreClient.fetchScore(eventId);
                } catch (Exception e) {
                    log.warn("External call attempt {} failed for eventId={}: {}", ctx.getRetryCount() + 1, eventId, e.toString());
                    throw e;
                }
            });

            if (score == null) {
                // Defensive: should not happen unless client returns null.
                log.warn("External API returned null for eventId={}", eventId);
                return;
            }

            LiveEventMessage message = new LiveEventMessage(score.getEventId(), score.getCurrentScore(), Instant.now());
            publisher.publish(message);
            log.debug("Job ok for eventId={}", eventId);

        } catch (Exception e) {
            // Do NOT cancel the job on a single failure; just log and try again next interval.
            log.error("Job failed for eventId={}: {}", eventId, e.toString());
        }
    }
}