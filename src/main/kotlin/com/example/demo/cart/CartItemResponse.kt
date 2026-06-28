package com.example.demo.cart

data class CartItemResponse(
    val cartId: String,
    val userId: String,
    val productId: String,
    val productName: String? = null,
    val quantity: Int,
    val unitPrice: Int,
    val lineTotal: Int,
    val sourcePage: String,
    val stockStatus: String? = null,
    val availableQuantity: Int? = null
)
