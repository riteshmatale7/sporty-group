package com.sportygroup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import java.time.Duration;

/**
 * Application-wide configuration.
 */
@Configuration
public class AppConfig {

  /**
   * Dedicated scheduler for per-event jobs.
   * Using a ThreadPoolTaskScheduler makes cancellation and per-event scheduling straightforward.
   */
  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(8);
    scheduler.setThreadNamePrefix("event-poller-");
    scheduler.setAwaitTerminationSeconds(5);
    scheduler.setWaitForTasksToCompleteOnShutdown(true);
    return scheduler;
  }

  /**
   * Retry template used for transient operations (HTTP, broker publishing).
   *
   * - retries: 3
   * - backoff: 250ms
   */
  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate template = new RetryTemplate();

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(3);

    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(250);

    template.setRetryPolicy(retryPolicy);
    template.setBackOffPolicy(backOffPolicy);
    return template;
  }
}
