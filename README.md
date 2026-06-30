# log-friends-examples

Spring Boot shopping mall demo for verifying the current `log-friends-sdk:v0.3.0` runtime flow against a local Log Friends Console.

The app runs on port `8081`, serves a shop UI at `/` and `/shop`, stores demo data in SQLite, and sends SDK data to Console on port `8080`.

## What This Example Shows

```text
Shop UI / REST API
  -> log-friends-sdk
  -> Agent registration
  -> Discovered LOG_EVENT candidates
  -> HTTP batch POST /ingest
  -> log-friends-console
  -> log-friends-console-web
```

The goal is to make Log Friends visible through a realistic service flow instead of isolated test endpoints.

## Demo Domains

| Domain | Endpoints | Main eventNames |
|---|---|---|
| Catalog | `GET /products`, `GET /products/{productId}` | `catalogProductsListed`, `catalogProductViewed` |
| Cart | `POST /carts/{cartId}/items`, `DELETE /carts/{cartId}/items/{productId}` | `cartItemAdded`, `cartItemRemoved` |
| Wishlist | `POST /wishlists/{wishlistId}/items`, `DELETE /wishlists/{wishlistId}/items/{productId}` | `wishlistItemAdded`, `wishlistItemRemoved` |
| Coupon | `POST /coupons/validate` | `couponValidated` |
| Order | `POST /orders`, `DELETE /orders/{orderId}`, `POST /orders/{orderId}/return-requests` | `orderCreated`, `orderCancelled`, `returnRequested` |
| Payment | `POST /payments`, `POST /payments/{transactionId}/refund` | `paymentProcessed`, `paymentRefunded` |
| Fulfillment | `POST /shipments`, `PUT /shipments/{shipmentId}/status` | `shipmentCreated`, `shipmentStatusChanged` |
| User | `POST /users`, `PUT /users/{userId}/deactivate` | `userRegistered`, `userDeactivated` |

The shop UI starts from product browsing and can generate cart, wishlist, coupon, order, payment, and shipment events.

## Expected Console Flow

Start Console backend first at `http://localhost:8080`.

On startup, the SDK registers the fixed `workerId` and `appName` as an Agent. After registration succeeds, SDK `v0.3.0` reports discovered `@LogEvent` candidates with `appVersion=examples-v0.3.0`.

```text
log-friends-examples
  -> POST /api/agents
  -> POST /api/agents/{agentId}/discovered-log-events
  -> POST /ingest
  -> Log Catalog / Raw Events / CSV in Console Web
```

The SDK does not auto-register confirmed LogSpecs. Confirmed LogSpecs are created or edited through Console APIs. After you use the shop UI or call the APIs, `LOG_EVENT` data is stored as Raw Events and becomes available for Log Catalog samples, mismatch checks, and CSV verification.

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

SQLite is used for inspectable local demo data:

```text
EXAMPLES_DATABASE_URL=jdbc:sqlite:build/log-friends-examples.sqlite
```

`schema.sql` and `data.sql` initialize product and order audit data. Tests use a separate test database setup.

Required JVM flags:

```text
-Djdk.attach.allowAttachSelf=true
-Dnet.bytebuddy.experimental=true
```

`bootRun` and `test` already set these flags in Gradle. Pass them explicitly when running the packaged JAR.

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
     -Dnet.bytebuddy.experimental=true \
     -jar build/libs/log-friends-examples.jar
```

If Console is not running, the app can still start, but SDK delivery and agent registration will log failures.

## Use The Shop UI

Open:

```text
http://localhost:8081/
```

Basic demo flow:

```text
Browse products
  -> view a product
  -> add to wishlist
  -> add to cart
  -> validate coupon
  -> create order
  -> process payment
  -> create shipment
  -> update shipment status
```

This flow generates `LOG_EVENT` records that can be checked in Console Web:

```text
http://localhost:3000/log-catalog
http://localhost:3000/raw-events
```

## Generate LOG_EVENT Data With curl

Product lookup:

```bash
curl http://localhost:8081/products
curl http://localhost:8081/products/PRD-SNK-001
```

Cart and wishlist:

```bash
curl -X POST http://localhost:8081/carts/CART-PORTFOLIO/items \
  -H 'Content-Type: application/json' \
  -d '{"userId":"USR-1","productId":"PRD-SNK-001","quantity":2,"sourcePage":"product-detail"}'

curl -X POST http://localhost:8081/wishlists/WISH-PORTFOLIO/items \
  -H 'Content-Type: application/json' \
  -d '{"userId":"USR-1","productId":"PRD-SNK-001","sourcePage":"product-card"}'
```

Coupon, order, payment, and shipment:

```bash
curl -X POST http://localhost:8081/coupons/validate \
  -H 'Content-Type: application/json' \
  -d '{"userId":"USR-1","couponCode":"WELCOME10","orderTotal":99000}'

curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PRD-SNK-001","quantity":2,"userId":"USR-1","customerEmail":"buyer@example.com","couponCode":"WELCOME10","orderTotal":178000,"channel":"WEB"}'

curl -X POST http://localhost:8081/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-PORTFOLIO-1","amount":50000,"method":"CARD"}'

curl -X POST http://localhost:8081/shipments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-PORTFOLIO-1","carrier":"CJ_LOGISTICS","trackingNumber":"TRK-PORTFOLIO-1","shipmentStatus":"READY_TO_SHIP","warehouseCode":"WH-SEOUL"}'
```

`OrderRequest.customerEmail` and selected user fields are masked as `__MASKED__` by SDK annotations.

## Verify In Console

1. Confirm Agent registration with `GET http://localhost:8080/api/agents`.
2. Open Console Web at `http://localhost:3000`.
3. Open Log Catalog and confirm discovered `LOG_EVENT` hints.
4. Use the shop UI or curl commands to create real `LOG_EVENT` records.
5. Open Raw Events and filter by `appName=order-service`, `workerId=order-service-local-1`, or `eventName`.
6. Download CSV from Raw Events to verify the selected date range.

Raw Events API endpoints:

```text
GET /api/events/custom
GET /api/events/custom.csv
```

Both endpoints accept `appName`, `workerId`, `eventName`, `from`, and `to`; the JSON endpoint also accepts `limit`.

## LOG_EVENT Contract Notes

Example `@LogEvent` names use camelCase. Invalid eventNames are skipped by the SDK and leave a warning in the target app log.

Parameter names become top-level `LOG_EVENT.payload` keys. DTO parameters remain object values; the SDK does not flatten request DTOs by default.

Console owns Agent records, Raw Event storage, LogSpec confirmation, Log Catalog assembly, mismatch calculation, Field Request state, and CSV export.

## Tests

```bash
./gradlew test
```

Controller and service tests cover the shop domains with SDK disabled where needed. Repository tests cover the local JDBC path used by the demo data.

## Related Docs

- [Example Log Catalog setup](docs/log-catalog.md)
- `../log-friends-sdk/README.md`
- `../log-friends-console/README.md`
- `../log-friends-console-web/README.md`
- `../docs/system/runtime-flow.md`
