package main

import (
	"fmt"
	"log"
	"net/http"
	"os"

	"github.com/log-freind/log-friends-examples/go/handler"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8084"
	}

	mux := http.NewServeMux()

	// Order routes
	mux.HandleFunc("POST /orders", handler.CreateOrder)
	mux.HandleFunc("DELETE /orders/{orderId}", handler.CancelOrder)

	// Payment routes
	mux.HandleFunc("POST /payments", handler.CreatePayment)
	mux.HandleFunc("POST /payments/{txId}/refund", handler.RefundPayment)

	// User routes
	mux.HandleFunc("POST /users", handler.CreateUser)
	mux.HandleFunc("PUT /users/{userId}/deactivate", handler.DeactivateUser)

	addr := fmt.Sprintf(":%s", port)
	log.Printf("Starting server on %s", addr)
	if err := http.ListenAndServe(addr, mux); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}
