# REST Calls Service (Backend Engineer Home Assignment)

This project implements a Java microservice that:

- Exposes `POST /events/status` to mark an event as `LIVE` / `NOT_LIVE`.
- For each `LIVE` event, schedules a job that runs every 10 seconds (configurable) and calls an external REST API.
- Transforms the external API response into a message and publishes it.
- Provides basic logging, error handling, and tests (JUnit + Cucumber).

The assignment brief this project follows is included as a PDF in the bundle. fileciteturn0file0

## Tech stack
- Java **18** (build + run)
- Spring Boot 3
- Maven
- WireMock (external API mocking)
- Kafka publishing (optional at runtime via profile), in-memory/log publish by default
- Testing: JUnit 5, Spring Boot Test, Cucumber (JUnit Platform), RestAssured

## Quick start

### 1) Start WireMock on **port 8091** (required)
This repo includes a `wiremock/` folder with a `mappings/` file that returns **HTTP 200** for:

`GET /api/events/{eventId}`

Using Docker:

```bash
docker compose up -d wiremock
```

Verify:

```bash
curl http://localhost:8091/api/events/1234
```

### 2) Run the service
Default mode (no Kafka required; messages are logged):

```bash
mvn spring-boot:run
```

Send a status update:

```bash
curl -X POST http://localhost:8080/events/status \
  -H 'Content-Type: application/json' \
  -d '{"eventId":"1234","status":"LIVE"}'
```

You should see logs like:
- scheduling created
- external call success
- message published (to logs)

### 3) Run with Kafka publishing (optional)
Spin up Kafka:

```bash
docker compose up -d kafka
```

Run the service using the `kafka` profile:

```bash
mvn -Dspring-boot.run.profiles=kafka spring-boot:run
```

Configuration (env vars):
- `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:9092`)
- `KAFKA_TOPIC` (default `live-events`)

## Configuration
`application.yaml` exposes these `app.*` settings:
- `app.external-api-base-url` (default `http://localhost:8091`)
- `app.polling-interval-ms` (default `10000`)
- `app.kafka-topic` (default `live-events`)

## Tests
Run all tests:

```bash
mvn test
```

Included tests:
- **JUnit** unit tests for the REST endpoint and scheduling behavior.
- **Cucumber** BDD scenarios:
  - Marking an event LIVE eventually publishes a message.
  - Marking an event NOT_LIVE stops publishing.

Cucumber tests spin up an in-process WireMock and override the poll interval to keep tests fast.

## Design decisions (short)
- **Per-event scheduling**: implemented with `TaskScheduler` and a `ConcurrentHashMap<eventId, ScheduledFuture<?>>` for idempotent schedule/cancel.
- **In-memory state**: `ConcurrentHashMap.newKeySet()` is sufficient for this prototype.
- **External calls**: Spring `WebClient` with a short timeout.
- **Retry logic**: `RetryTemplate` around external calls and (Kafka) publishing.
- **Publishing**: `MessagePublisher` abstraction with:
  - `KafkaMessagePublisher` (profile `kafka`)
  - `LogMessagePublisher` (default, no broker needed)

## WireMock mappings (port 8091)
The `wiremock/mappings/get-event-score.json` mapping uses response templating so it can respond for **any** event id.

## AI usage
This repository was drafted with AI assistance and then reviewed/edited to ensure:
- Java 18 compatibility
- runnable build (`mvn test`, `mvn spring-boot:run`)
- clear separation of concerns, comments, and configuration
- requirements coverage (endpoint, scheduling, external REST call, publish, tests)



## Running the dummy external API (WireMock) manually (port 8091)

This project includes a standalone WireMock configuration under `./wiremock/`.

1) Download a WireMock standalone jar (e.g. `wiremock-standalone-*.jar`) and place it anywhere on your machine.

2) From the project root, start WireMock on **port 8091** and point it to the local `wiremock/` folder:

```bash
java -jar /path/to/wiremock-standalone.jar --port 8091 --root-dir ./wiremock --global-response-templating
```

- Mappings are loaded from `./wiremock/mappings`
- Response bodies (if used) are loaded from `./wiremock/__files`

3) Smoke test the stub:

```bash
curl -i http://localhost:8091/api/events/testEvent
```

You should get HTTP 200 and a JSON body.

