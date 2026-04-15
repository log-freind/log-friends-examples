from pydantic import BaseModel


class UserRequest(BaseModel):
    name: str
    email: str


class UserResponse(BaseModel):
    userId: str
    status: str


class UserDeactivateResponse(BaseModel):
    userId: str
    status: str
