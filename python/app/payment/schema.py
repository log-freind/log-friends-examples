from pydantic import BaseModel


class PaymentRequest(BaseModel):
    orderId: str
    amount: float
    method: str


class PaymentResponse(BaseModel):
    txId: str
    status: str


class RefundResponse(BaseModel):
    txId: str
    status: str
    reason: str
