package com.example.demo.payment

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    fun processPayment(@RequestBody request: PaymentRequest): ResponseEntity<String> {
        val txId = paymentService.processPayment(request.orderId, request.amount, request.method)
        return ResponseEntity.ok(txId)
    }

    @PostMapping("/{transactionId}/refund")
    fun refund(
        @PathVariable transactionId: String,
        @RequestParam reason: String
    ): ResponseEntity<Void> {
        paymentService.refund(transactionId, reason)
        return ResponseEntity.noContent().build()
    }
}
