# Example Log Catalog Setup

`log-friends-examples` does not register LogSpecs from SDK runtime code.

Use Console APIs to connect example `LOG_EVENT` Raw Events to Log Catalog:

```text
@LogEvent in examples
  -> SDK HTTP JSON POST /ingest
  -> Console custom_events Raw Event
  -> Console Agent + LogSpec API
  -> Log Catalog Recent Sample + Mismatch + Field Request
```

## 1. Register The Example Agent

Register the fixed workerId before checking Log Catalog:

```text
workerId=order-service-local-1
appName=order-service
```

```bash
curl -X POST http://localhost:8082/api/agents \
  -H 'Content-Type: application/json' \
  -d '{
    "workerId": "order-service-local-1",
    "appName": "order-service",
    "sdkVersion": "1.2.0",
    "javaVersion": "21",
    "hostname": "local"
  }'
```

The response contains an `id`. Use it as `<agentId>` below.

## 2. Upsert Example LogSpecs

The current Console API stores LogSpec snapshots by Agent:

```bash
curl -X PUT http://localhost:8082/api/log-specs/by-agent/<agentId> \
  -H 'Content-Type: application/json' \
  -d '{
    "specs": [
      {
        "name": "orderCreated",
        "description": "Order creation business eventName",
        "levels": ["INFO"],
        "category": "BUSINESS",
        "fields": [
          {"name":"request","type":"JSON","required":true,"description":"OrderRequest DTO object. SDK keeps DTO parameters as object values instead of flattening fields"}
        ]
      },
      {
        "name": "paymentProcessed",
        "description": "Payment processed business eventName",
        "levels": ["INFO"],
        "category": "BUSINESS",
        "fields": [
          {"name":"orderId","type":"STRING","required":true},
          {"name":"amount","type":"INTEGER","required":true},
          {"name":"method","type":"STRING","required":true}
        ]
      },
      {
        "name": "userRegistered",
        "description": "User registration business eventName",
        "levels": ["INFO"],
        "category": "AUDIT",
        "fields": [
          {"name":"name","type":"STRING","required":true},
          {"name":"email","type":"STRING","required":true,"description":"SDK sends __MASKED__"}
        ]
      }
    ]
  }'
```

Backend owners define LogSpec and eventName contracts. Data engineers inspect Recent Sample and Mismatch, then create Field Requests when the contract needs another field.

## 3. Trigger Example Samples

Run the app with the same workerId:

```bash
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
./gradlew bootRun --args='--server.port=8081'
```

Trigger three `LOG_EVENT` samples:

```bash
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"PROD-1","quantity":2,"userId":"USR-1"}'

curl -X POST http://localhost:8081/payments \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","amount":50000,"method":"CARD"}'

curl -X POST http://localhost:8081/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Jane","email":"jane@example.com"}'
```

## 4. Inspect Log Catalog

Open the static UI:

```text
http://localhost:8082/log-catalog
```

Or query the API:

```bash
curl 'http://localhost:8082/api/log-catalog/apps/order-service/events?workerId=order-service-local-1&sampleSize=5'
```

Check:

- `orderCreated`, `paymentProcessed`, and `userRegistered` are connected by eventName.
- Recent Sample payloads come from `custom_events`.
- `userRegistered.email` is masked before display.
- Events emitted without a registered LogSpec appear as `NO_SPEC`.

## 5. See A Mismatch

Add a required top-level `couponId` field to the `orderCreated` LogSpec and upsert it again without changing the example app. The next Catalog response should show a `MISSING_FIELD` mismatch because the sample payload has top-level `request`, not `couponId`.

Field Request belongs to Console state. This example guide does not create Jira, Linear, or GitHub tickets.
