package com.sportygroup.cucumber;

import com.sportygroup.config.InMemoryTestPublisher;
import com.sportygroup.model.LiveEventMessage;
import com.sportygroup.publish.MessagePublisher;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for live event scenarios.
 */
public class LiveEventSteps {

    @LocalServerPort
    int port;


    MessagePublisher publisher;

    @Before
    void before() {
        // reset between scenarios
        WireMock.configureFor("localhost", CucumberSpringConfiguration.WIREMOCK.port());
        CucumberSpringConfiguration.WIREMOCK.resetAll();
        ((InMemoryTestPublisher) publisher).clear();
    }

    @After
    void after() {
        ((InMemoryTestPublisher) publisher).clear();
    }

    @Given("the service is running")
    public void theServiceIsRunning() {
        assertTrue(port > 0);
    }

    @Given("the external API returns a score for event {string}")
    public void externalApiReturnsScore(String eventId) {
        // Stub WireMock for this specific eventId
        CucumberSpringConfiguration.WIREMOCK.stubFor(get(urlEqualTo("/api/events/" + eventId)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{\"eventId\":\"" + eventId + "\",\"currentScore\":\"0:0\"}")));
    }

    @When("I mark event {string} as LIVE")
    public void markEventLive(String eventId) {
        given().baseUri("http://localhost").port(port).contentType("application/json").body("{\"eventId\":\"" + eventId + "\",\"status\":\"LIVE\"}").when().post("/events/status").then().statusCode(202);
    }

    @When("I mark event {string} as NOT_LIVE")
    public void markEventNotLive(String eventId) {
        given().baseUri("http://localhost").port(port).contentType("application/json").body("{\"eventId\":\"" + eventId + "\",\"status\":\"NOT_LIVE\"}").when().post("/events/status").then().statusCode(202);
    }

    @Then("a message should eventually be published for event {string}")
    public void messageEventuallyPublished(String eventId) {
        InMemoryTestPublisher p = (InMemoryTestPublisher) publisher;

        Instant deadline = Instant.now().plus(Duration.ofSeconds(2));
        while (Instant.now().isBefore(deadline)) {
            List<LiveEventMessage> msgs = p.getMessages();
            if (msgs.stream().anyMatch(m -> eventId.equals(m.getEventId()))) {
                return;
            }
            sleep(50);
        }
        fail("No message published for event " + eventId + " within timeout. Messages=" + p.getMessages().size());
    }

    @Then("no new messages should be published for event {string} within {int} second")
    public void noNewMessagesPublished(String eventId, int seconds) {
        InMemoryTestPublisher p = (InMemoryTestPublisher) publisher;

        long before = p.getMessages().stream().filter(m -> eventId.equals(m.getEventId())).count();
        sleep(seconds * 1000L);
        long after = p.getMessages().stream().filter(m -> eventId.equals(m.getEventId())).count();

        assertEquals(before, after, "Expected no additional messages after NOT_LIVE");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
