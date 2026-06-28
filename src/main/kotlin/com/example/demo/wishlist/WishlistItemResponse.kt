package com.example.demo.wishlist

data class WishlistItemResponse(
    val wishlistId: String,
    val userId: String,
    val productId: String,
    val sourcePage: String
)
