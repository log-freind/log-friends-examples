package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type CreatePaymentRequest struct {
	OrderID string  `json:"orderId"`
	Amount  float64 `json:"amount"`
	Method  string  `json:"method"`
}

type CreatePaymentResponse struct {
	TxID   string `json:"txId"`
	Status string `json:"status"`
}

type RefundPaymentResponse struct {
	TxID   string `json:"txId"`
	Status string `json:"status"`
	Reason string `json:"reason"`
}

func CreatePayment(w http.ResponseWriter, r *http.Request) {
	var req CreatePaymentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid request body", http.StatusBadRequest)
		return
	}

	resp := CreatePaymentResponse{
		TxID:   fmt.Sprintf("TX-%d", time.Now().UnixMilli()),
		Status: "PROCESSED",
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(resp)
}

func RefundPayment(w http.ResponseWriter, r *http.Request) {
	txID := r.PathValue("txId")
	reason := r.URL.Query().Get("reason")

	resp := RefundPaymentResponse{
		TxID:   txID,
		Status: "REFUNDED",
		Reason: reason,
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}
