# log-friends examples

log-friends SDK 연동 예제 모음. 각 언어별 예제 앱은 Order / Payment / User 3개 도메인을 구현한다.

## 구조

| 디렉토리 | 언어 / 프레임워크 | 포트 | SDK 연동 |
|---|---|---|---|
| [`spring/`](spring/) | Kotlin + Spring Boot 3 | 8081 | log-friends-sdk (JVM Agent) |
| [`python/`](python/) | Python + FastAPI | 8082 | - |
| [`nest/`](nest/) | TypeScript + NestJS | 8083 | - |
| [`go/`](go/) | Go + net/http | 8084 | - |

> log-friends-sdk는 JVM 전용입니다. Python / NestJS / Go 예제는 동일한 비즈니스 도메인을 다른 언어로 구현한 파트너 서비스 예제입니다.

## 빠른 시작

```bash
# Spring (JVM Agent 포함)
cd spring && ./gradlew bootRun

# Python
cd python && pip install -r requirements.txt && uvicorn app.main:app --port 8082

# NestJS
cd nest && npm install && npm run start

# Go
cd go && go run main.go
```

## API 공통 엔드포인트

| 도메인 | 메서드 | 경로 | 설명 |
|---|---|---|---|
| Order | POST | `/orders` | 주문 생성 |
| Order | DELETE | `/orders/{id}` | 주문 취소 |
| Payment | POST | `/payments` | 결제 처리 |
| Payment | POST | `/payments/{id}/refund` | 환불 |
| User | POST | `/users` | 회원 가입 |
| User | PUT | `/users/{id}/deactivate` | 계정 비활성화 |
