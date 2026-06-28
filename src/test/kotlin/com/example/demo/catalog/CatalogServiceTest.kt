package com.example.demo.catalog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CatalogServiceTest {
    private val catalogService = CatalogService(
        productCatalogRepository = FakeProductCatalogRepository(
            listOf(
                ProductResponse(
                    productId = "PRD-SNK-001",
                    name = "Daily Runner Sneakers",
                    category = "shoes",
                    price = 89000,
                    stockStatus = "IN_STOCK",
                    brand = "Northline",
                    rating = 4.7
                ),
                ProductResponse(
                    productId = "PRD-BAG-014",
                    name = "City Commuter Backpack",
                    category = "bags",
                    price = 129000,
                    stockStatus = "LOW_STOCK",
                    brand = "UrbanTrail",
                    rating = 4.5
                ),
                ProductResponse(
                    productId = "PRD-ELC-042",
                    name = "Noise Canceling Earbuds",
                    category = "electronics",
                    price = 159000,
                    stockStatus = "SOLD_OUT",
                    brand = "SoundPeak",
                    rating = 4.8
                )
            )
        )
    )

    @Test
    fun `listProducts - 기본 상품 목록은 쇼핑몰 필드를 포함`() {
        val products = catalogService.listProducts(null, null, null, null, null)

        assertThat(products).extracting("productId")
            .contains("PRD-SNK-001", "PRD-BAG-014", "PRD-ELC-042")
        assertThat(products).allSatisfy { product ->
            assertThat(product.category).isNotBlank()
            assertThat(product.price).isPositive()
            assertThat(product.stockStatus).isNotBlank()
        }
    }

    @Test
    fun `listProducts - category와 stockStatus로 필터링`() {
        val products = catalogService.listProducts(null, "bags", "LOW_STOCK", null, null)
        val product = products.single()

        assertThat(product.productId).isEqualTo("PRD-BAG-014")
        assertThat(product.category).isEqualTo("bags")
        assertThat(product.stockStatus).isEqualTo("LOW_STOCK")
    }

    @Test
    fun `getProduct - productId로 상품 상세 조회`() {
        val product = catalogService.getProduct("PRD-SNK-001")

        assertThat(product).isNotNull
        assertThat(product?.name).isEqualTo("Daily Runner Sneakers")
        assertThat(product?.price).isEqualTo(89000)
    }

    @Test
    fun `listProducts - 검색어와 가격 범위로 필터링`() {
        val products = catalogService.listProducts("runner", null, null, 50_000, 100_000)
        val product = products.single()

        assertThat(product.productId).isEqualTo("PRD-SNK-001")
        assertThat(product.price).isBetween(50_000, 100_000)
    }

    private class FakeProductCatalogRepository(
        private val products: List<ProductResponse>
    ) : ProductCatalogRepository {
        override fun findProducts(
            query: String?,
            category: String?,
            stockStatus: String?,
            minPrice: Int?,
            maxPrice: Int?
        ): List<ProductResponse> {
            return products
                .filter {
                    query == null ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.brand.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
                .filter { category == null || it.category.equals(category, ignoreCase = true) }
                .filter { stockStatus == null || it.stockStatus.equals(stockStatus, ignoreCase = true) }
                .filter { minPrice == null || it.price >= minPrice }
                .filter { maxPrice == null || it.price <= maxPrice }
        }

        override fun findByProductId(productId: String): ProductResponse? {
            return products.firstOrNull { it.productId == productId }
        }
    }
}
