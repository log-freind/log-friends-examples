package com.example.demo.payment

data class PaymentRequest(val orderId: String, val amount: Int, val method: String)
