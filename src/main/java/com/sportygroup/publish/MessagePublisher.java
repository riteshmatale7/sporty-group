package com.sportygroup.publish;

import com.sportygroup.model.LiveEventMessage;

/**
 * Abstraction for message publication.
 * <p>
 * - Production: KafkaMessagePublisher (profile: kafka)
 * - Local/dev: LogMessagePublisher (profile: local)
 * - Tests: InMemoryTestPublisher (test configuration)
 */
public interface MessagePublisher {
    void publish(LiveEventMessage message);
}
