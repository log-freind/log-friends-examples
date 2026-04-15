package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type CreateUserRequest struct {
	Name  string `json:"name"`
	Email string `json:"email"`
}

type CreateUserResponse struct {
	UserID string `json:"userId"`
	Status string `json:"status"`
}

type DeactivateUserResponse struct {
	UserID string `json:"userId"`
	Status string `json:"status"`
}

func CreateUser(w http.ResponseWriter, r *http.Request) {
	var req CreateUserRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	resp := CreateUserResponse{
		UserID: fmt.Sprintf("USR-%d", time.Now().UnixMilli()),
		Status: "ACTIVE",
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(resp)
}

func DeactivateUser(w http.ResponseWriter, r *http.Request) {
	userID := r.PathValue("userId")

	resp := DeactivateUserResponse{
		UserID: userID,
		Status: "INACTIVE",
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}
