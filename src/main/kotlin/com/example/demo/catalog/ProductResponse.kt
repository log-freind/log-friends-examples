package com.example.demo.catalog

data class ProductResponse(
    val productId: String,
    val name: String,
    val category: String,
    val price: Int,
    val stockStatus: String,
    val stockQuantity: Int = 0,
    val brand: String,
    val rating: Double,
    val imageUrl: String? = null,
    val description: String? = null
)
