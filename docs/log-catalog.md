# Shopping Mall Log Catalog Example

`log-friends-examples` is a Spring Boot shopping mall sample used to show how the SDK reports `LOG_EVENT` candidates, annotation-based LogSpec hints, and Raw Event samples to Console.

The sample now follows a small commerce flow:

```text
User
-> Product catalog
-> Wishlist
-> Cart
-> Coupon validation
-> Order
-> Payment
-> Shipment
-> Return / Refund / Cancellation
```

Product catalog rows are loaded from local SQLite seed data, so the shop example can be changed by editing SQL seed data instead of Kotlin code.

Cart pricing and stock checks are owned by the backend. The browser can send a client-observed price, but `CartService` recalculates `unitPrice`, `lineTotal`, and available quantity from the SQLite product catalog. The shop screen also treats checkout as a full-cart flow: coupon validation and order creation use the cart subtotal, and `POST /orders` can receive `items` line data for the products in the cart.

## Runtime Flow

```text
@LogEvent in examples
  -> SDK Agent handshake POST /api/agents
  -> SDK discovered candidates POST /api/agents/{agentId}/discovered-log-events
  -> SDK HTTP JSON POST /ingest when methods execute
  -> Console custom_events Raw Event
  -> Console API
  -> Console Web Log Catalog / Raw Events
```

## Run Examples

```bash
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_APP_NAME=order-service \
LOGFRIENDS_APP_VERSION=examples-v0.3.0 \
LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest \
./gradlew bootRun --args='--server.port=8081'
```

Console backend:

```text
http://localhost:8080
```

Console web:

```text
http://localhost:3000
```

Shopping mall screen:

```text
http://localhost:8081/shop
```

The screen loads products through `GET /products`, saves wishlist items through `POST /wishlists/{wishlistId}/items`, validates coupons through `POST /coupons/validate`, adds cart items through `POST /carts/{cartId}/items`, creates orders through `POST /orders`, and can continue to payment and shipment APIs. Those clicks create realistic `LOG_EVENT` samples for Console Raw Events.

## EventName Candidates

| Domain | eventName | API | Purpose |
|---|---|---|---|
| Catalog | `catalogProductsListed` | `GET /products` | Product list lookup with category and stock filters |
| Catalog | `catalogProductViewed` | `GET /products/{productId}` | Product detail lookup |
| Wishlist | `wishlistItemAdded` | `POST /wishlists/{wishlistId}/items` | Customer saves a product to wishlist |
| Wishlist | `wishlistItemRemoved` | `DELETE /wishlists/{wishlistId}/items/{productId}` | Customer removes a product from wishlist |
| Cart | `cartItemAdded` | `POST /carts/{cartId}/items` | Customer adds a product to cart |
| Cart | `cartItemRemoved` | `DELETE /carts/{cartId}/items/{productId}` | Customer removes a product from cart |
| Coupon | `couponValidated` | `POST /coupons/validate` | Customer validates a coupon before checkout |
| Order | `orderCreated` | `POST /orders` | Customer places an order |
| Order | `orderCancelled` | `DELETE /orders/{orderId}` | Customer or operator cancels an order |
| Order | `returnRequested` | `POST /orders/{orderId}/return-requests` | Customer requests a return |
| Payment | `paymentProcessed` | `POST /payments` | Payment is processed for an order |
| Payment | `paymentRefunded` | `POST /payments/{transactionId}/refund` | Payment transaction is refunded |
| Fulfillment | `shipmentCreated` | `POST /shipments` | Shipment is created for a paid order |
| Fulfillment | `shipmentStatusChanged` | `PUT /shipments/{shipmentId}/status` | Shipment status changes |
| User | `userRegistered` | `POST /users` | Customer account is registered |
| User | `userDeactivated` | `PUT /users/{userId}/deactivate` | Customer account is deactivated |

## Trigger Shopping Mall Samples

### 1. Register a user

```bash
curl -X POST http://localhost:8081/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Jane","email":"jane@example.com"}'
```

### 2. Browse products

```bash
curl 'http://localhost:8081/products?category=shoes&stockStatus=IN_STOCK'

curl 'http://localhost:8081/products?q=runner&minPrice=50000&maxPrice=100000'

curl http://localhost:8081/products/PRD-SNK-001
```

### 3. Save a product to wishlist

```bash
curl -X POST http://localhost:8081/wishlists/WISH-100/items \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"USR-001",
    "productId":"PRD-SNK-001",
    "sourcePage":"product-card"
  }'
```

### 4. Add a product to cart

```bash
curl -X POST http://localhost:8081/carts/CART-100/items \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"USR-001",
    "productId":"PRD-SNK-001",
    "quantity":2,
    "unitPrice":89000,
    "sourcePage":"product-detail"
  }'
```

### 5. Validate a coupon

```bash
curl -X POST http://localhost:8081/coupons/validate \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"USR-001",
    "couponCode":"WELCOME10",
    "orderTotal":178000
  }'
```

### 6. Create an order

```bash
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "productId":"PRD-SNK-001",
    "quantity":2,
    "userId":"USR-001",
    "customerEmail":"buyer@example.com",
    "couponCode":"WELCOME10",
    "channel":"MOBILE_APP",
    "orderTotal":227000,
    "deliveryMethod":"EXPRESS",
    "shippingZipCode":"06236",
    "items":[
      {
        "productId":"PRD-SNK-001",
        "quantity":2,
        "unitPrice":89000,
        "lineTotal":178000
      },
      {
        "productId":"PRD-HOM-033",
        "quantity":1,
        "unitPrice":69000,
        "lineTotal":69000
      }
    ]
  }'
```

### 7. Process a payment

```bash
curl -X POST http://localhost:8081/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","amount":227000,"method":"CARD"}'
```

### 8. Create and update a shipment

```bash
curl -X POST http://localhost:8081/shipments \
  -H 'Content-Type: application/json' \
  -d '{
    "orderId":"ORD-1001",
    "carrier":"CJ_LOGISTICS",
    "trackingNumber":"CJ-924812341",
    "shipmentStatus":"READY_TO_SHIP",
    "warehouseCode":"WH-SEOUL-01"
  }'

curl -X PUT http://localhost:8081/shipments/SHP-1001/status \
  -H 'Content-Type: application/json' \
  -d '{
    "shipmentStatus":"IN_TRANSIT",
    "warehouseCode":"HUB-DAEJEON-01",
    "carrier":"CJ_LOGISTICS"
  }'
```

### 9. Request a return and refund

```bash
curl -X POST http://localhost:8081/orders/ORD-1001/return-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "userId":"USR-001",
    "productId":"PRD-SNK-001",
    "reason":"size mismatch",
    "opened":true
  }'

curl -X POST 'http://localhost:8081/payments/TX-1001/refund?reason=size+mismatch'
```

## Inspect Console

Use Console Web first:

```text
http://localhost:3000
```

Or query Console backend directly:

```bash
curl 'http://localhost:8080/api/log-catalog/apps/order-service/events?workerId=order-service-local-1&sampleSize=5'
```

Check:

- Discovered `LOG_EVENT` candidates appear after startup, even before every eventName has a sample.
- LogSpec Hints show annotation-provided API and field descriptions.
- Raw Events show actual shopping mall payloads.
- `customerEmail` and `email` are masked before transport.
- Cart checkout samples include `items`, `orderTotal`, `couponCode`, delivery method, and shipping zip code.
- Product, wishlist, cart, coupon, order, payment, fulfillment, and user events are connected by commerce keys such as `userId`, `productId`, `orderId`, `transactionId`, and `shipmentId`.

## Notes

- This app is still an SDK usage example, not a full commerce backend.
- The goal is to provide realistic eventName and payload samples for Log Catalog and Raw Events.
- Backend owners define confirmed LogSpecs in Console. SDK annotations provide discovery hints before LogSpec is confirmed.
- Product catalog seed data lives in `src/main/resources/data.sql` and is stored in the local SQLite database configured by `EXAMPLES_DATABASE_URL`.
