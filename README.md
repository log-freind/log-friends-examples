# Log Friends Examples

**상품을 조회하고 Console에서 실제 수집 결과를 확인할 수 있는 쇼핑몰 예제**입니다.
SDK 설정, 이벤트 선언, 전송, 저장, 계약 확인까지 한 흐름으로 체험할 수 있습니다.

현재 의존성은 Kotlin SDK **1.1.0**입니다.
상품·장바구니·찜·쿠폰·주문·결제·배송·사용자 예제를 포함하며, 실제 결제 서비스가 아닌 데모입니다.

```text
상품 조회 → @LogEvent 메서드 → SDK 큐 → Console → Raw Events / Log Catalog
```

## 실행하기

JDK 21이 필요합니다. 먼저 [Console](https://github.com/log-freind/log-friends-console)을
`http://localhost:8080`에서 실행하세요.

```bash
LOGFRIENDS_INGEST_URL=http://localhost:8080/ingest \
LOGFRIENDS_WORKER_ID=order-service-local-1 \
LOGFRIENDS_APP_NAME=order-service \
LOGFRIENDS_BATCH_SIZE=50 \
./gradlew bootRun --args='--server.port=8081'
```

브라우저에서 [http://localhost:8081/](http://localhost:8081/)을 열고 상품을 조회합니다.
쇼핑 화면은 `/shop`에서도 열 수 있습니다.

**현재 Console은 요청당 50건까지 받습니다. 예제의 기본 배치 설정 100을 위 환경변수로 덮어써야 합니다.**
`bootRun`에는 ByteBuddy에 필요한 JVM 옵션이 설정되어 있습니다.

## 수집됐는지 확인하기

1. 상품 목록을 조회합니다: `curl http://localhost:8081/products`
2. [Console Web](https://github.com/log-freind/log-friends-console-web)을 실행하고 `http://localhost:3000/raw-events`를 엽니다.
3. `appName=order-service`, `eventName=catalogProductsListed`로 조회합니다.
4. Log Catalog에서 코드 힌트와 실제 payload를 비교합니다.

시작 시 SDK가 Agent와 발견된 이벤트 정의를 보고합니다.
**발견된 정의는 확정된 LogSpec이 아닙니다.** 계약 등록은 [Log Catalog 설정 안내](docs/log-catalog.md)를 참고하세요.

## 코드에서 볼 곳

| 디렉터리 | 확인할 내용 |
|---|---|
| [catalog](src/main/kotlin/com/example/demo/catalog) | 상품 조회와 `catalogProductsListed` 이벤트 |
| [애플리케이션 코드](src/main/kotlin/com/example/demo) | 장바구니·주문 등 도메인별 이벤트 선언 |
| [application.properties](src/main/resources/application.properties) | SDK 연결과 로컬 DB 설정 |

`@LogEvent`는 이벤트 이름과 설명, `@LogField`는 인자 의미와 필수 여부,
`@LogMasked`는 민감한 값의 마스킹을 지정합니다.
DTO 인자는 payload 안에 객체로 들어가며 자동으로 평탄화되지 않습니다.

## 설정

| 환경변수 | 용도 / 기본값 |
|---|---|
| `LOGFRIENDS_INGEST_URL` | Console 수집 주소, `http://localhost:8080/ingest` |
| `LOGFRIENDS_WORKER_ID` | `order-service-local-1` |
| `LOGFRIENDS_APP_NAME` | 미설정 시 Spring 앱 이름 `order-service` |
| `LOGFRIENDS_BATCH_SIZE` | 기본 100, 현재 연동에는 **50** 사용 |
| `LOGFRIENDS_BATCH_INTERVAL_MS` | 500ms |
| `LOGFRIENDS_QUEUE_CAPACITY` | 10,000건 |
| `LOGFRIENDS_QUEUE_MEMORY_BUDGET_BYTES` | 32MiB 추정 메모리 예산 |
| `EXAMPLES_DATABASE_URL` | `jdbc:sqlite:build/log-friends-examples.sqlite` |

같은 인스턴스를 이어서 관찰하려면 `workerId`를 유지하세요.
여러 인스턴스를 동시에 실행한다면 서로 다른 `workerId`를 지정하세요.
예제 서비스 데이터는 SQLite에, 수집 이벤트는 Console의 PostgreSQL/TimescaleDB에 저장됩니다.

## 빌드와 테스트

```bash
./gradlew build
```

패키징한 JAR를 직접 실행할 때는 앞의 SDK 환경변수를 지정하고 다음 JVM 옵션을 전달하세요.

```bash
java -Djdk.attach.allowAttachSelf=true \
     -Dnet.bytebuddy.experimental=true \
     -jar build/libs/log-friends-examples.jar
```

Console이 없으면 등록·전송이 실패합니다.
큐 적재나 SDK의 HTTP 전송 성공만으로 저장 완료를 판단하지 말고 Raw Events에서 확인하세요.

[Kotlin SDK](https://github.com/log-freind/log-friends-kt-sdk) ·
[Console](https://github.com/log-freind/log-friends-console) ·
[Console Web](https://github.com/log-freind/log-friends-console-web) · [Apache-2.0](LICENSE)
