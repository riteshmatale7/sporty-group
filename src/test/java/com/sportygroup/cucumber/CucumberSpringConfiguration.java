package com.sportygroup.cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.sportygroup.config.TestPublisherConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Bootstraps Spring for Cucumber.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestPublisherConfig.class)
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


}
