import time

from fastapi import APIRouter

from app.user.schema import UserDeactivateResponse, UserRequest, UserResponse

router = APIRouter(prefix="/users", tags=["user"])


@router.post("", response_model=UserResponse)
def create_user(body: UserRequest) -> UserResponse:
    user_id = f"USR-{int(time.time() * 1000)}"
    return UserResponse(userId=user_id, status="ACTIVE")


@router.put("/{userId}/deactivate", response_model=UserDeactivateResponse)
def deactivate_user(userId: str) -> UserDeactivateResponse:
    return UserDeactivateResponse(userId=userId, status="INACTIVE")
