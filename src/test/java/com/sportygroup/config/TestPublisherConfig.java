package com.sportygroup.config;

import com.sportygroup.publish.MessagePublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestPublisherConfig {

    /**
     * Override the real publisher with an in-memory one so we can assert messages.
     */
    @Bean
    @Primary
    public MessagePublisher testPublisher() {
        return new InMemoryTestPublisher();
    }
}
