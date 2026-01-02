package com.sportygroup.publish.impl;

import com.sportygroup.model.LiveEventMessage;
import com.sportygroup.publish.MessagePublisher;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default publisher for local development: logs the messages.
 * <p>
 * This keeps the service runnable without spinning up Kafka, while still keeping a Kafka implementation.
 */
@Component
@Profile("!kafka")
@Log4j2
public class LogMessagePublisherImpl implements MessagePublisher {

    @Override
    public void publish(LiveEventMessage message) {
        log.info("(local publish) {}", messageToString(message));
    }

    private String messageToString(LiveEventMessage message) {
        return "LiveEventMessage{eventId='" + message.getEventId() + "', currentScore='" + message.getCurrentScore() + "', " +
                "observedAt=" + message.getObservedAt() + "}";
    }
}
