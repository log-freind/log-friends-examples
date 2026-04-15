from pydantic import BaseModel


class OrderRequest(BaseModel):
    productId: str
    quantity: int
    userId: str


class OrderResponse(BaseModel):
    orderId: str
    status: str


class OrderCancelResponse(BaseModel):
    orderId: str
    status: str
    reason: str
