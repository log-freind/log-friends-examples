# log-friends-examples / go

Go `net/http` 예제 서버. Order / Payment / User 3개 도메인을 표준 라이브러리만으로 구현합니다.

## 실행 방법

### 로컬

```bash
go run main.go
```

### Docker

```bash
docker build -t log-friends-go .
docker run -p 8084:8084 log-friends-go
```

## 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `PORT` | `8084` | 서버 포트 |

## API 엔드포인트

### Order

| Method | Path | Body / Query | 응답 |
|--------|------|--------------|------|
| `POST` | `/orders` | `{"productId": string, "quantity": int, "userId": string}` | `{"orderId": "ORD-{ts}", "status": "CREATED"}` |
| `DELETE` | `/orders/{orderId}` | query: `reason` | `{"orderId": string, "status": "CANCELLED", "reason": string}` |

### Payment

| Method | Path | Body / Query | 응답 |
|--------|------|--------------|------|
| `POST` | `/payments` | `{"orderId": string, "amount": float64, "method": string}` | `{"txId": "TX-{ts}", "status": "PROCESSED"}` |
| `POST` | `/payments/{txId}/refund` | query: `reason` | `{"txId": string, "status": "REFUNDED", "reason": string}` |

### User

| Method | Path | Body | 응답 |
|--------|------|------|------|
| `POST` | `/users` | `{"name": string, "email": string}` | `{"userId": "USR-{ts}", "status": "ACTIVE"}` |
| `PUT` | `/users/{userId}/deactivate` | — | `{"userId": string, "status": "INACTIVE"}` |

## 예제 요청

```bash
# 주문 생성
curl -X POST http://localhost:8084/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"P001","quantity":2,"userId":"U001"}'

# 주문 취소
curl -X DELETE "http://localhost:8084/orders/ORD-1234567890?reason=customer_request"

# 결제 생성
curl -X POST http://localhost:8084/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-1234567890","amount":29900,"method":"CARD"}'

# 환불
curl -X POST "http://localhost:8084/payments/TX-1234567890/refund?reason=defective_item"

# 사용자 생성
curl -X POST http://localhost:8084/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com"}'

# 사용자 비활성화
curl -X PUT http://localhost:8084/users/USR-1234567890/deactivate
```
