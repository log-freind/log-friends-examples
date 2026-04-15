import time

from fastapi import APIRouter

from app.payment.schema import PaymentRequest, PaymentResponse, RefundResponse

router = APIRouter(prefix="/payments", tags=["payment"])


@router.post("", response_model=PaymentResponse)
def process_payment(body: PaymentRequest) -> PaymentResponse:
    tx_id = f"TX-{int(time.time() * 1000)}"
    return PaymentResponse(txId=tx_id, status="PROCESSED")


@router.post("/{txId}/refund", response_model=RefundResponse)
def refund_payment(txId: str, reason: str = "") -> RefundResponse:
    return RefundResponse(txId=txId, status="REFUNDED", reason=reason)
