# log-friends-examples

Spring Boot example application for `log-friends-sdk`. It demonstrates HTTP, Logback, JDBC, `@Service` method trace, and `@LogEvent` capture in a small domain-style app.

## Current Flow

```text
log-friends-examples
  -> log-friends-sdk
  -> HTTP POST /ingest
  -> log-friends-console
  -> TimescaleDB
```

Kafka, Protobuf transport, and a separate java-agent JAR are not used in the current example flow.

## Domains

| Domain | Endpoint examples | Captured signals |
|--------|-------------------|------------------|
| Order | `POST /orders`, `DELETE /orders/{id}` | HTTP, `@LogEvent`, service trace |
| Payment | `POST /payments`, refund API | HTTP, `@LogEvent`, service trace |
| User | `POST /users`, deactivate API | HTTP, `@LogEvent`, service trace |

## Run Full Stack

Start infrastructure and console:

```bash
cd /Users/choeseonghyeon/Desktop/log-friends
docker compose -f docker-compose.infra.yml up -d
docker compose -f docker-compose.platform.yml up -d --build
```

Run the example app:

```bash
cd /Users/choeseonghyeon/Desktop/log-friends/log-friends-examples
LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
./gradlew bootRun --args='--server.port=8081'
```

If running a packaged jar:

```bash
./gradlew bootJar
LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
java -Djdk.attach.allowAttachSelf=true \
     -jar build/libs/log-friends-examples-*.jar
```

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `order-service` | Example app name |
| `server.port` | `8081` | Example app port |
| `logfriends.ingest.url` | `http://localhost:8082/ingest` | Console ingest endpoint |

Environment variable:

| Variable | Description |
|----------|-------------|
| `LOGFRIENDS_INGEST_URL` | Console ingest endpoint |

## Example `@LogEvent`

```kotlin
@Service
class OrderService {
    @LogEvent("order.created")
    fun create(productId: String, quantity: Int, userId: String): String {
        // Captured by log-friends-sdk.
    }
}
```

## Tests

```bash
./gradlew test
```

The test task enables JVM attach options required by ByteBuddy.

## Related Docs

- `../log-friends-sdk/README.md`
- `../log-friends-console/README.md`
- `../docs/system/runtime-flow.md`
- `../docs/console/ingest-api.md`
