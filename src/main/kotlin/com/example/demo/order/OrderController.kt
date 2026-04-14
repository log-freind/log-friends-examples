package com.example.demo.order

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun create(@RequestBody request: OrderRequest): ResponseEntity<String> {
        val orderId = orderService.create(request.productId, request.quantity, request.userId)
        return ResponseEntity.ok(orderId)
    }

    @DeleteMapping("/{orderId}")
    fun cancel(@PathVariable orderId: String, @RequestParam reason: String): ResponseEntity<Void> {
        orderService.cancel(orderId, reason)
        return ResponseEntity.noContent().build()
    }
}
