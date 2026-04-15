# Log Friends NestJS Example

Order / Payment / User 3개 도메인을 포함한 NestJS TypeScript 예제 앱입니다.

## 실행 방법

### 개발 모드
```bash
npm install
npm run start:dev
```

### 빌드 후 실행
```bash
npm install
npm run build
npm start
```

### Docker
```bash
docker build -t log-friends-nest .
docker run -p 8083:8083 log-friends-nest
```

## 환경 변수

| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `PORT` | `8083` | 서버 포트 |

## API 엔드포인트

### Order

| Method | Path | Body / Query | Response |
|--------|------|--------------|----------|
| POST | `/orders` | `{"productId": string, "quantity": number, "userId": string}` | `{"orderId": "ORD-...", "status": "CREATED"}` |
| DELETE | `/orders/:orderId` | query: `reason?` | `{"orderId": string, "status": "CANCELLED", "reason": string}` |

### Payment

| Method | Path | Body / Query | Response |
|--------|------|--------------|----------|
| POST | `/payments` | `{"orderId": string, "amount": number, "method": string}` | `{"txId": "TX-...", "status": "PROCESSED"}` |
| POST | `/payments/:txId/refund` | query: `reason?` | `{"txId": string, "status": "REFUNDED", "reason": string}` |

### User

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/users` | `{"name": string, "email": string}` | `{"userId": "USR-...", "status": "ACTIVE"}` |
| PUT | `/users/:userId/deactivate` | — | `{"userId": string, "status": "INACTIVE"}` |
