# AGENTS.md — log-friends-examples

## 책임

`log-friends-examples`는 `log-friends-sdk` 사용법을 보여주는 Spring Boot 예제 앱이다. Order, Payment, User 도메인에서 `@LogEvent`와 SDK 계측 흐름을 확인한다.

## 실행 기준

```bash
./gradlew build
./gradlew bootJar

LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest \
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_APP_NAME=order-service \
LOGFRIENDS_APP_VERSION=examples-v0.3.0 \
java -Djdk.attach.allowAttachSelf=true \
     -jar build/libs/log-friends-examples.jar
```

## 주의사항

- Console 없이도 앱은 기동할 수 있지만 이벤트 전송은 실패 로그가 남을 수 있다.
- `workerId`는 실행마다 바뀌는 자동값이 아니라 설정에서 고정 주입한다.
- SDK `v0.3.0` 기준으로 Agent handshake 성공 후 Discovered `LOG_EVENT` 후보가 Console로 보고된다.
- 테스트 환경에서는 `logfriends.agent.enabled=false` 권장.
- 예제는 SDK 사용 패턴을 보여주는 목적이며 Console 저장/통계/Log Catalog 책임을 갖지 않는다.
