package com.sportygroup.client;

import com.sportygroup.model.ExternalScoreResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * HTTP client for the external REST API.
 */
@Component
public class ExternalScoreClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalScoreClient.class);

    private WebClient webClient;
    private final Duration timeout;

    @Value("${app.external-api-base-url}")
    private String baseUrl;

    @Value("${app.external-api-path-template}")
    private String pathTemplate;

    @Value("${app.external-api-timeout-ms}")
    private long timeoutMs;


    public ExternalScoreClient(WebClient.Builder builder) {
        // WebClient is non-blocking; in this assignment we call .block() inside a scheduler thread.
        // That's acceptable here and keeps dependencies minimal.
        this.webClient = builder.baseUrl(baseUrl).build();
        this.pathTemplate = pathTemplate;
        this.timeout = Duration.ofMillis(2000); //TODO
    }

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    /**
     * Fetch score information for a given event.
     * <p>
     * Endpoint shape: GET /api/events/{eventId}
     */
    public ExternalScoreResponse fetchScore(String eventId) {
        try {
            return webClient.get().uri(pathTemplate, eventId).retrieve().onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class).defaultIfEmpty("").flatMap(body -> Mono.error(new RuntimeException("External API error: HTTP " + resp.statusCode() + " body=" + body)))).bodyToMono(ExternalScoreResponse.class).timeout(timeout).block();
        } catch (WebClientResponseException e) {
            log.warn("External API call failed: status={}, body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.warn("External API call failed: {}", e.toString());
            throw e;
        }
    }
}
