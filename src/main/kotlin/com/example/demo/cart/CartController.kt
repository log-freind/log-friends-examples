package com.example.demo.cart

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/carts")
class CartController(private val cartService: CartService) {

    @PostMapping("/{cartId}/items")
    fun addItem(
        @PathVariable cartId: String,
        @RequestBody request: CartItemRequest
    ): ResponseEntity<CartItemResponse> {
        val item = cartService.addItem(
            cartId = cartId,
            userId = request.userId,
            productId = request.productId,
            quantity = request.quantity,
            requestedUnitPrice = request.unitPrice,
            sourcePage = request.sourcePage
        )

        return ResponseEntity.ok(item)
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    fun removeItem(
        @PathVariable cartId: String,
        @PathVariable productId: String,
        @RequestParam userId: String,
        @RequestParam(defaultValue = "cart-page") sourcePage: String
    ): ResponseEntity<Void> {
        cartService.removeItem(
            cartId = cartId,
            userId = userId,
            productId = productId,
            sourcePage = sourcePage
        )

        return ResponseEntity.noContent().build()
    }
}
