package com.sportygroup.publish.impl;

import com.sportygroup.model.LiveEventMessage;
import com.sportygroup.publish.MessagePublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka-based publisher (profile: kafka).
 */
@Component
@Profile("kafka")
@Log4j2
public class KafkaMessagePublisherImpl implements MessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RetryTemplate retryTemplate;

    @Value("${app.kafka-topic:live-events}")
    private String topic;

    public KafkaMessagePublisherImpl(KafkaTemplate<String, String> kafkaTemplate,
                                     ObjectMapper objectMapper,

                                     RetryTemplate retryTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void publish(LiveEventMessage message) {
        retryTemplate.execute(ctx -> {
            try {
                String payload = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(topic, message.getEventId(), payload).get();
                log.info("Published to Kafka topic={} key={} payload={}", topic, message.getEventId(), payload);
                return null;
            } catch (Exception e) {
                log.warn("Kafka publish attempt {} failed: {}", ctx.getRetryCount() + 1, e.toString());
                throw new RuntimeException(e);
            }
        });
    }
}
