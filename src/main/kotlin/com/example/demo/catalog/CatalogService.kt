package com.example.demo.catalog

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CatalogService(
    private val productCatalogRepository: ProductCatalogRepository
) {
    private val log = LoggerFactory.getLogger(CatalogService::class.java)

    @LogEvent(
        name = "catalogProductsListed",
        description = "Catalog product list lookup business eventName",
        apiMethod = "GET",
        apiPath = "/products",
        apiDescription = "Lists shopping mall products with optional category and stock filters"
    )
    fun listProducts(
        @LogField(description = "Optional product search keyword", type = "STRING")
        query: String?,
        @LogField(description = "Optional product category filter such as shoes", type = "STRING")
        category: String?,
        @LogField(description = "Optional product stock status filter such as IN_STOCK", type = "STRING")
        stockStatus: String?,
        @LogField(description = "Optional minimum product price filter", type = "INT", required = false)
        minPrice: Int?,
        @LogField(description = "Optional maximum product price filter", type = "INT", required = false)
        maxPrice: Int?
    ): List<ProductResponse> {
        log.info(
            "Listing products, query: {}, category: {}, stockStatus: {}, minPrice: {}, maxPrice: {}",
            query,
            category,
            stockStatus,
            minPrice,
            maxPrice
        )
        return productCatalogRepository.findProducts(query, category, stockStatus, minPrice, maxPrice)
    }

    @LogEvent(
        name = "catalogProductViewed",
        description = "Catalog product detail lookup business eventName",
        apiMethod = "GET",
        apiPath = "/products/{productId}",
        apiDescription = "Looks up one shopping mall product by productId"
    )
    fun getProduct(
        @LogField(description = "Viewed product identifier", type = "STRING")
        productId: String
    ): ProductResponse? {
        log.info("Viewing product detail {}", productId)
        return productCatalogRepository.findByProductId(productId)
    }
}
