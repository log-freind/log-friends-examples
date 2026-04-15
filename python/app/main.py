import os

from fastapi import FastAPI

from app.order.router import router as order_router
from app.payment.router import router as payment_router
from app.user.router import router as user_router

app = FastAPI(title="Log Friends Python Example", version="1.0.0")

app.include_router(order_router)
app.include_router(payment_router)
app.include_router(user_router)


@app.get("/health")
def health() -> dict:
    return {"status": "UP"}


if __name__ == "__main__":
    import uvicorn

    port = int(os.getenv("SERVER_PORT", "8082"))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=False)
