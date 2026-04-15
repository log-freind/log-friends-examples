# Log Friends — Python FastAPI Example

Order / Payment / User 3개 도메인을 구현한 FastAPI 예제 서버입니다.

## 실행 방법

### 로컬 실행

```bash
cd python
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8082
```

### Docker 실행

```bash
cd python
docker build -t log-friends-python .
docker run -p 8082:8082 log-friends-python
```

### 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `SERVER_PORT` | `8082` | 서버 포트 |

---

## API 엔드포인트

### Order

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/orders` | 주문 생성 |
| `DELETE` | `/orders/{orderId}?reason=` | 주문 취소 |

**POST /orders**
```json
// Request
{ "productId": "PROD-1", "quantity": 2, "userId": "USR-001" }

// Response
{ "orderId": "ORD-1713200000000", "status": "CREATED" }
```

**DELETE /orders/{orderId}**
```json
// Response
{ "orderId": "ORD-1713200000000", "status": "CANCELLED", "reason": "고객 요청" }
```

---

### Payment

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/payments` | 결제 처리 |
| `POST` | `/payments/{txId}/refund?reason=` | 환불 처리 |

**POST /payments**
```json
// Request
{ "orderId": "ORD-1713200000000", "amount": 15000.0, "method": "CARD" }

// Response
{ "txId": "TX-1713200000001", "status": "PROCESSED" }
```

**POST /payments/{txId}/refund**
```json
// Response
{ "txId": "TX-1713200000001", "status": "REFUNDED", "reason": "상품 불량" }
```

---

### User

| Method | Path | 설명 |
|---|---|---|
| `POST` | `/users` | 사용자 생성 |
| `PUT` | `/users/{userId}/deactivate` | 사용자 비활성화 |

**POST /users**
```json
// Request
{ "name": "홍길동", "email": "hong@example.com" }

// Response
{ "userId": "USR-1713200000002", "status": "ACTIVE" }
```

**PUT /users/{userId}/deactivate**
```json
// Response
{ "userId": "USR-1713200000002", "status": "INACTIVE" }
```

---

### Health Check

```
GET /health → { "status": "UP" }
```

---

## API 문서 (자동 생성)

서버 실행 후 `http://localhost:8082/docs` 에서 Swagger UI를 확인할 수 있습니다.
