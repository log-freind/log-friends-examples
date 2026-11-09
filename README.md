# Log Friends Examples

상품 조회부터 주문·결제·배송까지 SDK 이벤트 수집을 확인하는 Spring Boot 쇼핑몰 예제입니다. 실제 결제 서비스가 아닌 데모입니다.

```text
브라우저 / API -> Examples + Kotlin SDK -> Console -> Raw Events / Log Catalog
                         |
                         +-> SQLite (예제 서비스 데이터)
```

예제 서비스 데이터는 SQLite에, 수집 이벤트는 Console의 TimescaleDB/PostgreSQL에 저장됩니다.

> **개발 구현 기준 문서:** 최신 로컬 코드의 사용법을 설명합니다. 이 README 전용 PR에는 제품 코드·배포 변경이 포함되지 않습니다. GitHub 기본 브랜치만 clone한 경우 아래 기능·명령이 아직 일치하지 않을 수 있으며, 관련 코드 반영 후 사용해야 합니다.

## 처음 실행하기

JDK 21이 필요합니다. 현재 앱은 Spring Boot **4.0.8**, SDK 의존성은 **`com.logfriends:log-friends-sdk:1.1.1`**입니다. 아래 명령은 해당 개발 소스가 준비된 작업공간 기준입니다.

### 1. Console과 앱 키 준비

[Console](https://github.com/log-freind/log-friends-console)을 `http://localhost:8082/logfriend`에서 실행하고 **로그인 -> 앱 등록 -> 서버 수집 키 발급** 순서로 준비하세요. 앱 이름·고정 Worker ID·버전은 Console에서 설정합니다.
키 하나는 Worker 하나에 대응하므로 같은 키를 사용하는 앱을 동시에 여러 개 실행하지 마세요.

### 2. 현재 SDK 연결

[Kotlin SDK](https://github.com/log-freind/log-friends-kt-sdk) 저장소에서 실행합니다.

```bash
./gradlew publishToMavenLocal
```

현재 개발 SDK는 공개 모델 20개를 기능별 `model` 패키지로 이동해 이전 import·바이너리와 호환을 보장하지 않습니다. 같은 `1.1.1`이라도 오래된 로컬 아티팩트는 다시 설치하고 소비자를 재컴파일해야 합니다.

### 3. 키 설정 후 Examples 실행

Examples 저장소에서 실행합니다. 아래 키 자리에는 Console 발급 키를 실행 환경으로 주입하고 Git·채팅에 올리지 마세요.

```bash
export LOGFRIENDS_APP_KEY='<issued server key>'
export LOGFRIENDS_ENV=test
./gradlew bootRun
```

[쇼핑 화면](http://localhost:8081/)에서 상품을 조회하세요. `/shop`도 같은 화면입니다.
`bootRun`에는 ByteBuddy용 JVM 옵션이 설정되어 있습니다.

로컬에서는 `.env`에 키를 둘 수도 있습니다. `bootRun`만 `.env`를 읽어 자식 JVM에 전달하며, 비어 있지 않은 기존 환경변수가 우선합니다. `java -jar`·컨테이너는 키를 별도 주입해야 합니다.

| 설정 | 기본값 / 의미 |
|---|---|
| `LOGFRIENDS_APP_KEY` | Console 발급 서버 수집 키, 필수 |
| `LOGFRIENDS_ENV` | `test` (기본): 로컬 Console 8082, `nas`: 같은 namespace의 `http://log-friends-console:8080/logfriend` |
| `EXAMPLES_DATABASE_URL` | `jdbc:sqlite:build/log-friends-examples.sqlite` |

SDK는 시작 시 `GET /api/sdk/config`로 앱 이름·Worker·버전을 받습니다. 개별 URL·Worker·앱 이름 환경변수로 시작 설정을 덮어쓰지 않습니다.
설정 조회 실패 시 앱은 기동할 수 있지만 SDK는 비활성화됩니다. Console·키를 복구한 뒤 앱을 재시작하세요. Console의 앱 설정 변경도 재시작 후 반영됩니다.

## 수집 확인

1. SDK 시작 로그에서 Agent 등록 성공을 확인하고 API 수집 정책을 받아올 때까지 기다립니다. 정책 수신 전이거나 API 수집이 OFF이면 해당 MVC 요청은 수집하지 않습니다.
2. `curl http://localhost:8081/products`로 상품 목록을 조회합니다.
3. [Console Web](https://github.com/log-freind/log-friends-console-web)의 **Raw Events**에서 등록한 앱과 `eventName=catalogProductsListed`로 조회합니다.
4. **Log Catalog**에서 코드 설명과 실제 payload를 비교합니다. 발견된 정의는 힌트이며 LogSpec 계약 등록은 Console API에서 별도로 합니다.

SDK는 등록 시도 후 60초 간격으로 heartbeat를 보내며 성공 후 API 수집 정책을 갱신합니다. 동기 MVC 요청 범위의 OFF만 적용하며 비동기 작업으로 자동 전파하지 않습니다.
HTTP 배치는 기본·최대 **50건**, 전송 주기는 기본 **500ms**입니다. DB 저장 배치와는 별개입니다.

`/actuator/logfriends`의 `sent`는 HTTP 전송 성공, `stored`는 Console이 확인한 원본 저장입니다. `storageFailed`·`duplicates`·`unconfirmed`도 따로 확인하세요. 큐 적재·전송 성공·앱 Running만으로 저장 완료를 판단하지 마세요. SDK에는 영구 큐나 무손실 보장이 없습니다. 진단 endpoint는 외부에 공개하지 마세요.

## 코드에서 볼 곳

현재 개발 소스에서 다음 파일을 확인할 수 있습니다. 공개 main에 없는 파일은 링크하지 않습니다.

| 경로 (`src/main/` 기준) | 확인할 내용 |
|---|---|
| `kotlin/com/example/demo/catalog/CatalogService.kt` | `@LogEvent`·`@LogField`, 상품 조회 이벤트 |
| `kotlin/com/example/demo/order/OrderRequest.kt` | DTO payload와 `@LogMasked` 사용 |
| `kotlin/com/example/demo/` | 장바구니·찜·쿠폰·주문·결제·배송·사용자 예제 |
| `resources/application.properties` | 포트·SQLite·Actuator 설정 |

이벤트에는 정상 반환한 메서드의 인자가 담깁니다. 반환값이나 DB 커밋 완료를 뜻하지 않으며 DTO 인자는 자동으로 평탄화되지 않습니다. 필드 설명·필수 여부 힌트가 고객 입력 검증을 대신하지는 않습니다.

## 합성 트래픽과 학습 예제

Node.js 20 이상에서 먼저 시나리오만 확인합니다.

```bash
node scripts/synthetic-traffic.mjs --dry-run
# Examples와 Console을 실행한 데모 환경에서만 실제 API 호출
node scripts/synthetic-traffic.mjs
```

기본 대상은 `http://localhost:8081`이며 주문 20개에 API 96회를 호출합니다. 출력 이벤트 수는 **예상치**이지 저장 확인 결과가 아닙니다. 예제 DB와 Console에 데이터가 남고 자동 삭제하지 않으므로 운영 서비스에는 사용하지 마세요.
`orderCreated` payload에는 반환된 `orderId`가 없어 후속 이벤트와의 완전한 연결도 보장하지 않습니다.

- `scripts/seed-model-demo.mjs`는 과거 직접 주입 데모입니다. 현재 앱 키 인증 연결이 없어 최신 Console의 실제 주입 경로로 안내하지 않습니다. `--dry-run`과 단위 테스트만 사용할 수 있습니다.
- [고정 주문 CPU 학습](training/order-cpu/README.md), [생성·비교·개선 runner](training/generated-cpu/README.md), [호텔 예약 오프라인 실험](training/kaggle-hotel/README.md)은 별도 학습 예제입니다. 범용 AutoML·운영 예측 서빙 완료를 뜻하지 않습니다.
- 로컬 문서 `docs/kaggle-retail-scenario.md`에는 별도 소매 데이터 시나리오가 있습니다.

## 빌드와 검증

```bash
./gradlew build
./gradlew bootJar
java -Djdk.attach.allowAttachSelf=true \
     -Dnet.bytebuddy.experimental=true \
     -jar build/libs/log-friends-examples.jar
```

위 JAR 실행에도 앞의 환경변수가 필요합니다. SDK 저장소가 바로 옆에 있다면 `./gradlew --include-build ../log-friends-sdk build`로 오래된 Maven Local 대신 현재 소스를 연결할 수 있습니다.
일반 테스트는 SDK 계측을 끄고 실행하며 실서버 테스트는 기본 제외됩니다.

```bash
node --test scripts/synthetic-traffic.test.mjs scripts/seed-model-demo.test.mjs
```

실제 저장 검증은 로컬 문서 `docs/sdk-delivery-verification.md`를 따르세요. `node scripts/verify-sdk-delivery.mjs`는 `process.loadEnvFile`을 지원하는 Node.js와 실제 로컬 Console·발급 키가 필요하며, 정상 51건·잘못된 이벤트 1건을 **실제 DB에 남깁니다**. 같은 키를 쓰는 앱을 먼저 종료하세요. 이번 README 갱신이 실서버 검증 성공을 뜻하지는 않습니다.

로컬 문서 `docs/log-catalog.md`에는 계약 설정 예제가 있습니다. 상위 작업공간의 `docs/sdk/*`·`docs/console/*`도 **로컬 전용**이며 이 GitHub 저장소에 포함되지 않습니다.

[Kotlin SDK](https://github.com/log-freind/log-friends-kt-sdk) · [Console](https://github.com/log-freind/log-friends-console) · [Console Web](https://github.com/log-freind/log-friends-console-web) · [Apache-2.0](LICENSE)
