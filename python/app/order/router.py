import time

from fastapi import APIRouter

from app.order.schema import OrderCancelResponse, OrderRequest, OrderResponse

router = APIRouter(prefix="/orders", tags=["order"])


@router.post("", response_model=OrderResponse)
def create_order(body: OrderRequest) -> OrderResponse:
    order_id = f"ORD-{int(time.time() * 1000)}"
    return OrderResponse(orderId=order_id, status="CREATED")


@router.delete("/{orderId}", response_model=OrderCancelResponse)
def cancel_order(orderId: str, reason: str = "") -> OrderCancelResponse:
    return OrderCancelResponse(orderId=orderId, status="CANCELLED", reason=reason)
