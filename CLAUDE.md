# CLAUDE.md — log-friends-examples

## 개요
log-friends-sdk 사용법을 보여주는 Spring Boot 예제 앱. **Order / Payment / User** 3개 도메인으로 `@LogEvent` 어노테이션과 `LogSpec` DSL 활용 패턴을 시연한다.

## 핵심 스택
- Kotlin 2.3.20 / JVM 21
- Spring Boot 3.4.0 (web, actuator)
- log-friends-sdk: `com.github.log-freind:log-friends-sdk:v1.0.0` (JitPack)
- 서버 포트: 8081

## 빌드 & 실행
```bash
./gradlew build              # 빌드 + 테스트
./gradlew bootRun            # 로컬 실행 (포트 8081)
```

## 주요 파일
```
src/main/kotlin/com/example/demo/
├── order/
│   ├── OrderController.kt       # POST /orders, DELETE /orders/{orderId}
│   ├── OrderService.kt          # @LogEvent("order.created/cancelled")
│   └── OrderLogConfig.kt        # LogSpec.define("order.*") @Bean 정의
├── payment/
│   ├── PaymentController.kt     # POST /payments, POST /payments/{txId}/refund
│   ├── PaymentService.kt
│   └── PaymentLogConfig.kt
└── user/
    ├── UserController.kt        # POST /users, PUT /users/{userId}/deactivate
    ├── UserService.kt
    └── UserLogConfig.kt
src/main/resources/application.properties   # 포트 8081, Kafka 설정
```

## 주의사항
- SDK 의존성: JitPack(`https://jitpack.io`) 인터넷 접속 필요
- 필수 JVM 옵션: `-Djdk.attach.allowAttachSelf=true` (build.gradle.kts에 포함됨)
- 테스트 환경: `src/test/resources/application.properties`에 `logfriends.agent.enabled=false` 필요
- Kafka 없이 기동 가능 (이벤트 전송 실패 로그 출력됨)
- `LogSpec` Bean은 Spring Context 초기화 후 5초 딜레이로 스캔됨
- 커밋: 한글, `feat:`/`fix:`/`refactor:`/`docs:` prefix
