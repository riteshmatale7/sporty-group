package com.sportygroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the REST-calls service.
 *
 * Requirements covered:
 * - REST endpoint to toggle events live/not-live
 * - per-live-event scheduled job calling external REST API every N seconds
 * - transform response to message and publish to Kafka (or log-only in local profile)
 */
@SpringBootApplication
public class RestCallsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RestCallsServiceApplication.class, args);
  }
}
