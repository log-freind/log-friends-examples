# log-friends-examples

A comprehensive Spring Boot example application demonstrating the **log-friends-sdk** observability framework. This repository showcases how to instrument a multi-domain microservices application with structured logging, event tracking, and automatic interception of HTTP, JDBC, and method-level operations.

## Overview

The examples application is a domain-driven Spring Boot app implementing three core services:

- **Order Service** - Order creation and cancellation with structured event logging
- **Payment Service** - Payment processing and refund handling with transaction tracking
- **User Service** - User registration and account management with audit logging

All services automatically integrate with the log-friends-sdk agent to capture:
- HTTP requests/responses via `DispatcherServlet` interception
- Business events via `@LogEvent` annotations with custom `LogSpec` schemas
- Database operations via JDBC `PreparedStatement` interception
- Method performance tracking via `@Service` method trace interception (threshold: 10ms)
- Structured logs via Logback integration

## Key Features

### Domain-Driven Design
Each domain (order, payment, user) is organized in its own package with:
- `Controller` - REST API endpoints
- `Service` - Business logic with `@LogEvent` annotations
- `LogConfig` - `LogSpec` definitions for structured logging
- `Request` - DTO classes for API contracts

### Structured Logging with LogSpec
Each domain defines custom log schemas using the `LogSpec` builder pattern:

```kotlin
// Example from OrderLogConfig.kt
LogSpec.define("order.created")
    .description("주문 생성 이벤트")
    .level(LogLevel.INFO)
    .category(LogCategory.BUSINESS)
    .field("productId").type(String::class.java).required()
    .field("quantity").type(Int::class.java).required()
    .field("userId").type(String::class.java).required()
    .build()
```

### @LogEvent Annotations
Business-critical methods are marked with `@LogEvent` to trigger automatic event capture:

```kotlin
@Service
class OrderService {
    @LogEvent("order.created")
    fun create(productId: String, quantity: Int, userId: String): String { ... }
    
    @LogEvent("order.cancelled")
    fun cancel(orderId: String, reason: String) { ... }
}
```

The `@LogEvent` annotation is retained at runtime to enable the agent to intercept and log these method invocations.

### REST API Endpoints
All endpoints are automatically intercepted by the HTTP interceptor:

**Order Service** (`/orders`)
- `POST /orders` - Create new order
- `DELETE /orders/{orderId}` - Cancel order

**Payment Service** (`/payments`)
- `POST /payments` - Process payment
- `POST /payments/{txId}/refund` - Refund transaction

**User Service** (`/users`)
- `POST /users` - Register new user
- `PUT /users/{userId}/deactivate` - Deactivate account

### Database Operations
All JDBC `PreparedStatement` executions are automatically intercepted and logged by the agent.

## Architecture

```
┌─────────────────────────────────────────────┐
│     Spring Boot Application                 │
│     (log-friends-examples)                  │
└─────────────────────────────────────────────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌────────┐ ┌──────────┐ ┌──────────┐
│ Order  │ │ Payment  │ │ User     │
│Service │ │ Service  │ │ Service  │
├────────┤ ├──────────┤ ├──────────┤
│OrderC. │ │PaymentC. │ │UserC.    │
│OrderS. │ │PaymentS. │ │UserS.    │
│OrderLC │ │PaymentLC │ │UserLC    │
│OrderR. │ │PaymentR. │ │UserR.    │
└────────┘ └──────────┘ └──────────┘
    │        │        │
    └────────┼────────┘
             │
        ┌────▼────────────────┐
        │ log-friends-sdk     │
        │ (Agent Interceptors)│
        ├────────────────────┤
        │ HTTP Interception  │
        │ JDBC Interception  │
        │ LOG_EVENT          │
        │ METHOD_TRACE       │
        │ Logback Integration│
        └────┬───────────────┘
             │
        ┌────▼──────────────┐
        │ Async Queue       │
        │ (capacity: 10k)   │
        └────┬──────────────┘
             │
        ┌────▼──────────────────┐
        │ Kafka Batching        │
        │ (100 events / 500ms)  │
        └────┬──────────────────┘
             │
        ┌────▼──────────┐
        │ Kafka Topic   │
        │ log-friends   │
        │ .batch        │
        └───────────────┘
```

## Quick Start

### Prerequisites
- Java 21
- Kotlin 2.3.20
- Gradle 8.x
- Docker & Docker Compose (recommended for full stack)

### Local Development

#### 1. Build the SDK and Examples

```bash
cd /path/to/log-friends

# Build the SDK
./gradlew :log-friends-sdk:jar

# Build the examples application
./gradlew :examples:bootJar
```

#### 2. Build the Java Agent (required)

The examples app requires the log-friends Java agent:

```bash
./gradlew :java-agent:shadowJar
```

This creates a Shadow JAR with all dependencies at:
```
java-agent/build/libs/java-agent-all.jar
```

#### 3. Run with Agent

```bash
java -javaagent:../java-agent/build/libs/java-agent-all.jar \
  -Djdk.attach.allowAttachSelf=true \
  -Dnet.bytebuddy.experimental=true \
  -Dspring.application.name=order-service \
  -Dserver.port=8081 \
  -jar examples/build/libs/examples.jar
```

### Docker (Recommended)

The easiest way to run the complete log-friends stack including examples, Kafka, and storage:

```bash
cd /path/to/log-friends

# Build and run everything
docker compose up --build

# Examples app will be available at http://localhost:8081
```

## Configuration

### Environment Variables

The application respects the following environment variables (with defaults):

```bash
# Kafka brokers for agent communication
LOGFRIENDS_KAFKA_BROKERS=localhost:9092

# Spring Boot configuration
SPRING_APPLICATION_NAME=order-service
SERVER_PORT=8081
```

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=order-service
server.port=8081

# Kafka configuration (agent uses LOGFRIENDS_KAFKA_BROKERS env var)
logfriends.kafka.brokers=${LOGFRIENDS_KAFKA_BROKERS:localhost:9092}

# Logging levels
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
```

### Agent Parameters

The log-friends agent respects these parameters (see CLAUDE.md for details):

- `logfriends.batch.size=100` - Events per batch
- `logfriends.batch.interval.ms=500` - Batch send interval
- `logfriends.queue.capacity=10000` - Async queue size
- `logfriends.trace.threshold.ms=10` - @Service method threshold

## Domain Examples

### Order Flow

```
POST /orders
{
  "productId": "PROD-001",
  "quantity": 2,
  "userId": "user123"
}
↓
OrderController.create()
↓
OrderService.create() [@LogEvent("order.created")]
↓
Event logged with schema:
{
  "type": "order.created",
  "productId": "PROD-001",
  "quantity": 2,
  "userId": "user123"
}
↓
Kafka → Analytics Pipeline
```

### Payment Flow

```
POST /payments
{
  "orderId": "ORD-162334",
  "amount": 50000,
  "method": "CREDIT_CARD"
}
↓
PaymentController.processPayment()
↓
PaymentService.processPayment() [@LogEvent("payment.processed")]
↓
Event logged with schema:
{
  "type": "payment.processed",
  "orderId": "ORD-162334",
  "amount": 50000,
  "method": "CREDIT_CARD"
}
↓
Kafka → Analytics Pipeline
```

### User Management

```
POST /users
{
  "name": "홍길동",
  "email": "hong@example.com"
}
↓
UserController.register()
↓
UserService.register() [@LogEvent("user.registered")]
↓
Audit event logged:
{
  "type": "user.registered",
  "name": "홍길동",
  "email": "hong@example.com"
}
↓
Kafka → Audit Trail
```

## Testing

### Run All Tests

```bash
./gradlew test
```

Tests are configured to run with the ByteBuddy experimental flag:

```kotlin
tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-Djdk.attach.allowAttachSelf=true",
        "-Dnet.bytebuddy.experimental=true"
    )
}
```

### Example Test Files

- `OrderControllerTest.kt` - Order service REST API tests
- `PaymentControllerTest.kt` - Payment service tests
- `UserControllerTest.kt` - User service tests

Tests use `@WebMvcTest` for isolated controller testing with mocked services.

### Running Tests with Agent

To run tests with the full agent enabled (not recommended for unit tests):

```bash
# Build the agent first
./gradlew :java-agent:shadowJar

# Run tests with agent
./gradlew test -Pjavaagent=../java-agent/build/libs/java-agent-all.jar
```

## File Structure

```
log-friends-examples/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/demo/
│   │   │   ├── DemoApplication.kt          # Spring Boot entry point
│   │   │   ├── order/
│   │   │   │   ├── OrderController.kt      # REST endpoints
│   │   │   │   ├── OrderService.kt         # Business logic (@LogEvent)
│   │   │   │   ├── OrderLogConfig.kt       # LogSpec definitions
│   │   │   │   ├── OrderRequest.kt         # DTO
│   │   │   │   └── OrderRepository.kt      # Data access (JDBC intercepted)
│   │   │   ├── payment/
│   │   │   │   ├── PaymentController.kt
│   │   │   │   ├── PaymentService.kt
│   │   │   │   ├── PaymentLogConfig.kt
│   │   │   │   ├── PaymentRequest.kt
│   │   │   │   └── PaymentRepository.kt
│   │   │   └── user/
│   │   │       ├── UserController.kt
│   │   │       ├── UserService.kt
│   │   │       ├── UserLogConfig.kt
│   │   │       ├── UserRequest.kt
│   │   │       └── UserRepository.kt
│   │   └── resources/
│   │       └── application.properties       # Configuration
│   └── test/
│       └── kotlin/com/example/demo/
│           ├── order/
│           │   └── OrderControllerTest.kt
│           ├── payment/
│           │   └── PaymentControllerTest.kt
│           └── user/
│               └── UserControllerTest.kt
├── build.gradle.kts                        # Gradle configuration
├── Dockerfile                              # Container build
├── .env                                    # Environment variables
└── README.md                               # This file
```

## LogEvent Annotations & @Retention

All `@LogEvent` annotations in this application use `@Retention(AnnotationRetention.RUNTIME)`, which is required for the agent to intercept and log method invocations at runtime. This annotation is defined in the `log-friends-sdk`:

```kotlin
// From log-friends-sdk
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogEvent(val value: String)
```

## Interceptor Details

The log-friends agent automatically intercepts the following without code changes:

### HTTP Interceptor
- Captures all requests to `DispatcherServlet`
- Logs request method, path, status, and response time
- Triggered for all REST endpoints in controllers

### LOG_EVENT Interceptor
- Intercepts methods annotated with `@LogEvent`
- Maps method parameters to LogSpec fields
- Publishes structured events to Kafka

### METHOD_TRACE Interceptor
- Intercepts methods in classes annotated with `@Service`
- Only logs methods with execution time ≥ 10ms (configurable)
- Tracks method name, class, duration, and result

### JDBC Interceptor
- Intercepts `PreparedStatement` executions
- Captures SQL queries and parameter values
- Logs execution time and row counts

### Logback Integration
- Automatically captures all Logback log statements
- Integrates with structured log pipeline

## Related Documentation

- **[Architecture Guide](../docs/01-architecture.md)** - System design and components
- **[Communication Protocol](../docs/02-communication.md)** - Kafka message format and Protobuf schema
- **[Component Reference](../docs/03-components.md)** - Detailed component documentation
- **[Configuration Guide](../docs/04-configuration.md)** - All configuration options
- **[Architecture Decision Records](../docs/05-adr.md)** - Design decisions and rationale

## Related Repositories

- **[log-friends-sdk](../log-friends-sdk)** - Core SDK and annotations
- **[java-agent](../java-agent)** - ByteBuddy-based Java agent with Shadow JAR
- **[log-friends-pipeline](../log-friends-pipeline)** - Kafka consumer and Spark streaming
- **[log-friends-console](../log-friends-console)** - Web UI for viewing logs and events

## Development Notes

### Adding New Domains

To add a new domain service (e.g., Inventory Service):

1. Create `src/main/kotlin/com/example/demo/inventory/`
2. Define controller, service, and LogConfig
3. Add `@LogEvent` annotations on business methods
4. Define LogSpecs in LogConfig with `@Bean` and `@Configuration`
5. Add tests to `src/test/kotlin/com/example/demo/inventory/`

### Best Practices

- ✅ Always use `@LogEvent` on business-critical methods
- ✅ Define clear LogSpecs with required fields and examples
- ✅ Use appropriate `LogLevel` and `LogCategory` for each event
- ✅ Keep @Service methods >10ms for meaningful tracing
- ✅ Use structured LogSpecs instead of unstructured logging
- ❌ Don't modify the agent classes (they're in java-agent module)
- ❌ Don't hardcode Kafka brokers; use env variables
- ❌ Don't skip `@Retention(RUNTIME)` on custom annotations

## Troubleshooting

### Agent Not Intercepting Requests
- Ensure `-javaagent:path/to/java-agent-all.jar` is passed to Java
- Verify `LOGFRIENDS_KAFKA_BROKERS` is set and Kafka is running
- Check ByteBuddy flag: `-Dnet.bytebuddy.experimental=true`

### Events Not Appearing in Kafka
- Verify Kafka is running: `kafka-broker-api-versions.sh --bootstrap-server localhost:9092`
- Check agent logs in application stdout
- Ensure queue capacity and batch size are reasonable (default: 10k, 100)

### Tests Failing
- Run with ByteBuddy flags: `-Djdk.attach.allowAttachSelf=true -Dnet.bytebuddy.experimental=true`
- Tests use `@WebMvcTest` which doesn't load the full application context
- Mock the service layer if testing controllers in isolation

## License

This project is part of the log-friends observability framework.
