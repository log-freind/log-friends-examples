# log-friends-examples

Spring Boot example application for the first-phase `log-friends-sdk` flow.

The app demonstrates runtime capture for five SDK eventTypes:

| eventType | Example source |
|---|---|
| `HTTP` | Order, Payment, and User REST calls |
| `LOG` | Logback messages inside the services |
| `JDBC` | H2-backed `order_audit` insert during `POST /orders` |
| `METHOD_TRACE` | Spring `@Service` methods, with the example threshold set to `0ms` |
| `LOG_EVENT` | camelCase `@LogEvent` methods |

## Runtime Flow

```text
log-friends-examples
  -> log-friends-sdk
  -> HTTP JSON batch POST /ingest
  -> log-friends-console
  -> PostgreSQL / TimescaleDB
```

The example path uses the SDK library, HTTP ingest, and the Console without a separate java-agent JAR.

## Before Running

The SDK requires a fixed `workerId` and a Console ingest endpoint.

This branch uses the SDK `main` baseline that enforces required configuration, camelCase `LOG_EVENT.eventName`, and `@LogMasked` handling.

```bash
export LOGFRIENDS_WORKER_ID=order-service-local-1
export LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest
```

`workerId` identifies the running app instance. Do not generate a new value on every run if you want Console Agent metadata and statistics to stay connected.

`application.properties` keeps `order-service-local-1` and `http://localhost:8082/ingest` as local example fallbacks. Set the environment variables explicitly when verifying Console integration.

## Run With Console

Start a Console that can receive ingest requests at `http://localhost:8082/ingest` first.

Run the example app:

```bash
git clone https://github.com/log-freind/log-friends-examples.git
cd log-friends-examples
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
./gradlew bootRun --args='--server.port=8081'
```

Run a packaged JAR:

```bash
./gradlew bootJar
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
java -Djdk.attach.allowAttachSelf=true \
     -jar build/libs/log-friends-examples-*.jar
```

## Run Against A Mock Ingest Endpoint

Use the local harness when Console is not running:

```bash
python3 scripts/mock_ingest_server.py
```

In another terminal:

```bash
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_INGEST_URL=http://127.0.0.1:8089/ingest \
./gradlew bootRun --args='--server.port=8081'
```

The mock server prints each posted batch, its top-level `workerId`, and the eventType values inside `events`.

## Walkthrough

### 1. Order Creates HTTP, LOG, JDBC, METHOD_TRACE, and LOG_EVENT

```bash
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-1","quantity":2,"userId":"USR-1"}'
```

Expected `LOG_EVENT.eventName`: `orderCreated`

Expected top-level payload fields. The service receives `OrderRequest` as a DTO, so the SDK keeps it as one object value instead of flattening it:

```json
{
  "request": {
    "productId": "PROD-1",
    "quantity": 2,
    "userId": "USR-1"
  }
}
```

This flow also inserts one row into the in-memory H2 `order_audit` table so SDK JDBC capture has a real `PreparedStatement` path.

### 2. Order Cancel

```bash
curl -X DELETE 'http://localhost:8081/orders/ORD-1001?reason=changed-mind'
```

Expected `LOG_EVENT.eventName`: `orderCancelled`

### 3. Payment

```bash
curl -X POST http://localhost:8081/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","amount":50000,"method":"CARD"}'

curl -X POST 'http://localhost:8081/payments/TX-1001/refund?reason=order-cancelled'
```

Expected `LOG_EVENT.eventName` values: `paymentProcessed`, `paymentRefunded`

### 4. User And Masking

```bash
curl -X POST http://localhost:8081/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Jane","email":"jane@example.com"}'

curl -X PUT http://localhost:8081/users/USR-1001/deactivate
```

Expected `LOG_EVENT.eventName` values: `userRegistered`, `userDeactivated`

`UserService.register()` marks the email parameter with `@LogMasked`. The SDK sends that field as:

```json
{
  "email": "__MASKED__"
}
```

## LOG_EVENT Contract

Example `@LogEvent` names use camelCase:

```kotlin
@LogEvent("userRegistered")
fun register(name: String, @LogMasked email: String): String
```

The SDK skips invalid `LOG_EVENT.eventName` values and leaves a warning in the target app log. Dotted names such as `user.registered` are intentionally not used by this example.

Method parameter names become top-level `LOG_EVENT.payload` keys. DTOs are not flattened by the SDK contract. For example, `OrderService.create(request: OrderRequest)` is sent as top-level `request`.

## LogSpec And Log Catalog

This example app captures runtime data only. It does not auto-register LogSpec definitions.

Console owns Agent registration, LogSpec upsert, Raw Event storage, Log Catalog assembly, mismatch calculation, and Field Request state. Use [docs/log-catalog.md](docs/log-catalog.md) to register the example Agent and LogSpec payloads through Console APIs.

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.application.name` | `order-service` | Example app name |
| `server.port` | `8081` | Example app port |
| `logfriends.worker.id` | `order-service-local-1` | Fixed local Worker identifier |
| `logfriends.ingest.url` | `http://localhost:8082/ingest` | Console ingest endpoint |
| `logfriends.trace.threshold.ms` | `0` | Show METHOD_TRACE in short example calls |

| Environment variable | Description |
|---|---|
| `LOGFRIENDS_WORKER_ID` | Overrides `logfriends.worker.id` |
| `LOGFRIENDS_INGEST_URL` | Overrides `logfriends.ingest.url` |

## Tests

```bash
./gradlew test
```

Controller tests disable the SDK with `logfriends.agent.enabled=false`. `OrderAuditRepositoryTest` keeps the JDBC example path covered with the in-memory H2 database.

## Related Docs

- [Log Catalog setup](docs/log-catalog.md)
- `../log-friends-sdk/README.md`
- `../log-friends-console/README.md`
- `../docs/system/runtime-flow.md`
- `../docs/console/ingest-api.md`
