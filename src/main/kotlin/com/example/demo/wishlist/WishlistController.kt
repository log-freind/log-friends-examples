package com.example.demo.wishlist

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/wishlists")
class WishlistController(private val wishlistService: WishlistService) {

    @PostMapping("/{wishlistId}/items")
    fun addItem(
        @PathVariable wishlistId: String,
        @RequestBody request: WishlistItemRequest
    ): ResponseEntity<WishlistItemResponse> {
        val item = wishlistService.addItem(
            wishlistId = wishlistId,
            userId = request.userId,
            productId = request.productId,
            sourcePage = request.sourcePage
        )
        return ResponseEntity.ok(item)
    }

    @DeleteMapping("/{wishlistId}/items/{productId}")
    fun removeItem(
        @PathVariable wishlistId: String,
        @PathVariable productId: String,
        @RequestParam userId: String,
        @RequestParam(defaultValue = "wishlist-page") sourcePage: String
    ): ResponseEntity<Void> {
        wishlistService.removeItem(
            wishlistId = wishlistId,
            userId = userId,
            productId = productId,
            sourcePage = sourcePage
        )
        return ResponseEntity.noContent().build()
    }
}
