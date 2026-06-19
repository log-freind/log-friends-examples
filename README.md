# log-friends-examples

Spring Boot example app for verifying the current `log-friends-sdk:v0.3.0` runtime flow against a local Log Friends Console.

The app runs on port `8081` and sends SDK data to Console on port `8080`. It demonstrates:

| Domain | Endpoints | Main SDK behavior |
|---|---|---|
| Order | `POST /orders`, `DELETE /orders/{orderId}` | `HTTP`, `LOG`, `JDBC`, `METHOD_TRACE`, `LOG_EVENT`; `orderCreated`, `orderCancelled` |
| Payment | `POST /payments`, `POST /payments/{txId}/refund` | `LOG_EVENT`; `paymentProcessed`, `paymentRefunded` |
| User | `POST /users`, `PUT /users/{userId}/deactivate` | `LOG_EVENT`; `userRegistered`, `userDeactivated`, `@LogMasked` email |

The Order create flow also inserts into the in-memory H2 `order_audit` table so JDBC capture has a real `PreparedStatement` path.

## Expected Console Flow

Start Console first at `http://localhost:8080`.

```text
log-friends-examples
  -> SDK startup Agent registration POST /api/agents
  -> Discovered LOG_EVENT candidates POST /api/agents/{agentId}/discovered-log-events
  -> captured event batches POST /ingest
  -> Console Raw Events, CSV export, Log Catalog
```

On startup, the SDK registers the fixed `workerId` and `appName` as an Agent. After registration succeeds, SDK `v0.3.0` reports discovered `@LogEvent` candidates with `appVersion=examples-v0.3.0`.

In Console, Log Catalog can show Discovered `LOG_EVENT` candidates and annotation-based LogSpec hints before samples exist. The SDK does not auto-register confirmed LogSpecs; confirmed LogSpecs are created or edited through Console APIs. After you call the example endpoints, `LOG_EVENT` data is stored as Raw Events and becomes available for Log Catalog recent samples, mismatch checks, and Raw Events / CSV verification.

## Configuration

Use these values when verifying against local Console:

```bash
export LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest
export LOGFRIENDS_WORKER_ID=order-service-local-1
export LOGFRIENDS_APP_NAME=order-service
export LOGFRIENDS_APP_VERSION=examples-v0.3.0
```

Defaults in `src/main/resources/application.properties` match the same local setup except `LOGFRIENDS_APP_NAME`, which falls back to `spring.application.name=order-service`.

The fixed `workerId` is intentional. Do not generate a new value every run if you want Console Agent metadata, discovered candidates, Raw Events, and Log Catalog data to stay connected.

Required JVM flag:

```text
-Djdk.attach.allowAttachSelf=true
```

`bootRun` already sets this flag in Gradle. Pass it explicitly when running the packaged JAR.

## Build And Run

Build:

```bash
./gradlew build
./gradlew bootJar
```

Run with Console:

```bash
LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest \
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_APP_NAME=order-service \
LOGFRIENDS_APP_VERSION=examples-v0.3.0 \
./gradlew bootRun --args='--server.port=8081'
```

Run the packaged JAR:

```bash
./gradlew bootJar
LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest \
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_APP_NAME=order-service \
LOGFRIENDS_APP_VERSION=examples-v0.3.0 \
java -Djdk.attach.allowAttachSelf=true \
     -jar build/libs/log-friends-examples.jar
```

If Console is not running, the app can still start, but SDK delivery will log failures. For a lightweight receiver:

```bash
python3 scripts/mock_ingest_server.py
```

Then run the app with `LOGFRIENDS_INGEST_URL=http://127.0.0.1:8089/ingest`.

## Generate LOG_EVENT Data

Order:

```bash
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-1","quantity":2,"userId":"USR-1","customerEmail":"buyer@example.com","couponCode":"WELCOME10"}'

curl -X DELETE 'http://localhost:8081/orders/ORD-1001?reason=changed-mind'
```

Expected eventNames: `orderCreated`, `orderCancelled`.

`OrderService.create(request: OrderRequest)` sends one top-level payload field named `request`; DTO fields are not flattened. `OrderRequest.customerEmail` is masked as `__MASKED__`.

Payment:

```bash
curl -X POST http://localhost:8081/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","amount":50000,"method":"CARD"}'

curl -X POST 'http://localhost:8081/payments/TX-1001/refund?reason=order-cancelled'
```

Expected eventNames: `paymentProcessed`, `paymentRefunded`.

User:

```bash
curl -X POST http://localhost:8081/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Jane","email":"jane@example.com"}'

curl -X PUT http://localhost:8081/users/USR-1001/deactivate
```

Expected eventNames: `userRegistered`, `userDeactivated`.

`UserService.register()` masks the `email` parameter as `__MASKED__`.

## Verify In Console

1. Confirm Agent registration with `GET http://localhost:8080/api/agents`, or by seeing `order-service` in Log Catalog.
2. Open Log Catalog at `http://localhost:8080/log-catalog`.
3. Confirm Discovered `LOG_EVENT` candidates and LogSpec hints appear for the example eventNames.
4. Trigger the curl commands above to create real `LOG_EVENT` Raw Events.
5. Open Raw Events at `http://localhost:8080/raw-events?appName=order-service&workerId=order-service-local-1`.
6. Filter by `eventName` if needed, then use the CSV download to verify the full selected date range.

Raw Events API endpoints:

```text
GET /api/events/custom
GET /api/events/custom.csv
```

Both endpoints accept `appName`, `workerId`, `eventName`, `from`, and `to`; the JSON endpoint also accepts `limit`.

## LOG_EVENT Contract Notes

Example `@LogEvent` names use camelCase. Invalid eventNames are skipped by the SDK and leave a warning in the target app log.

Parameter names become top-level `LOG_EVENT.payload` keys. DTO parameters remain object values, so `OrderRequest` is sent under `request` instead of being flattened into separate top-level fields.

Console owns Agent records, Raw Event storage, LogSpec confirmation, Log Catalog assembly, mismatch calculation, Field Request state, and CSV export.

## Tests

```bash
./gradlew test
```

Controller tests disable the SDK with `logfriends.agent.enabled=false`. `OrderAuditRepositoryTest` covers the H2-backed JDBC example path.

## Related Docs

- [Example Log Catalog setup](docs/log-catalog.md)
- `../log-friends-sdk/README.md`
- `../log-friends-console/README.md`
- `../docs/system/runtime-flow.md`
