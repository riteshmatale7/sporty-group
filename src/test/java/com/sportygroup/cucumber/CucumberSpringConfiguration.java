package com.sportygroup.cucumber;

import com.sportygroup.config.InMemoryTestPublisher;
import com.sportygroup.publish.MessagePublisher;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Bootstraps Spring for Cucumber.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    // One WireMock per test JVM.
    public static final WireMockServer WIREMOCK = new WireMockServer(options().dynamicPort());

    static {
        WIREMOCK.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        // speed up scheduler for tests
        registry.add("app.polling-interval-ms", () -> "200");
        registry.add("app.external-api-base-url", () -> "http://localhost:" + WIREMOCK.port());
    }

    /**
     * Override the real publisher with an in-memory one so we can assert messages.
     */
    @Bean
    @Primary
    public MessagePublisher testPublisher() {
        return new InMemoryTestPublisher();
    }
}
