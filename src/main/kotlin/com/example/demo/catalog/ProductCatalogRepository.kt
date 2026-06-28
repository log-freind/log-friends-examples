package com.example.demo.catalog

interface ProductCatalogRepository {
    fun findProducts(
        query: String?,
        category: String?,
        stockStatus: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): List<ProductResponse>

    fun findByProductId(productId: String): ProductResponse?
}
