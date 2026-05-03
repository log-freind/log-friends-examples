# AGENTS.md — log-friends-examples

## 책임

`log-friends-examples`는 `log-friends-sdk` 사용법을 보여주는 Spring Boot 예제 앱이다. Order, Payment, User 도메인에서 `@LogEvent`와 SDK 계측 흐름을 확인한다.

## 실행 기준

```bash
./gradlew build
./gradlew bootJar

LOGFRIENDS_INGEST_URL=http://localhost:8082/ingest \
LOGFRIENDS_WORKER_ID=order-service-local-1 \
java -Djdk.attach.allowAttachSelf=true \
     -jar build/libs/examples.jar
```

## 주의사항

- Console 없이도 앱은 기동할 수 있지만 이벤트 전송은 실패 로그가 남을 수 있다.
- `workerId`는 실행마다 바뀌는 자동값이 아니라 설정에서 고정 주입한다.
- 테스트 환경에서는 `logfriends.agent.enabled=false` 권장.
- 예제는 SDK 사용 패턴을 보여주는 목적이며 Console 저장/통계/Log Catalog 책임을 갖지 않는다.
