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

    @PostMapping("/{txId}/refund")
    fun refund(@PathVariable txId: String, @RequestParam reason: String): ResponseEntity<Void> {
        paymentService.refund(txId, reason)
        return ResponseEntity.noContent().build()
    }
}
