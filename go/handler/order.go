package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type CreateOrderRequest struct {
	ProductID string `json:"productId"`
	Quantity  int    `json:"quantity"`
	UserID    string `json:"userId"`
}

type CreateOrderResponse struct {
	OrderID string `json:"orderId"`
	Status  string `json:"status"`
}

type CancelOrderResponse struct {
	OrderID string `json:"orderId"`
	Status  string `json:"status"`
	Reason  string `json:"reason"`
}

func CreateOrder(w http.ResponseWriter, r *http.Request) {
	var req CreateOrderRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	resp := CreateOrderResponse{
		OrderID: fmt.Sprintf("ORD-%d", time.Now().UnixMilli()),
		Status:  "CREATED",
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(resp)
}

func CancelOrder(w http.ResponseWriter, r *http.Request) {
	orderID := r.PathValue("orderId")
	reason := r.URL.Query().Get("reason")

	resp := CancelOrderResponse{
		OrderID: orderID,
		Status:  "CANCELLED",
		Reason:  reason,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}
