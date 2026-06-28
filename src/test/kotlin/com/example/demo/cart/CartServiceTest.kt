package com.example.demo.cart

import com.example.demo.catalog.ProductCatalogRepository
import com.example.demo.catalog.ProductResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class CartServiceTest {
    private val cartService = CartService(
        productCatalogRepository = FakeProductCatalogRepository(
            listOf(
                ProductResponse(
                    productId = "PRD-SNK-001",
                    name = "Daily Runner Sneakers",
                    category = "shoes",
                    price = 89000,
                    stockStatus = "IN_STOCK",
                    stockQuantity = 12,
                    brand = "Northline",
                    rating = 4.7
                ),
                ProductResponse(
                    productId = "PRD-ELC-042",
                    name = "Noise Canceling Earbuds",
                    category = "electronics",
                    price = 159000,
                    stockStatus = "SOLD_OUT",
                    stockQuantity = 0,
                    brand = "SoundPeak",
                    rating = 4.8
                )
            )
        )
    )

    @Test
    fun `addItem - 클라이언트 가격이 아니라 catalog 가격으로 계산`() {
        val item = cartService.addItem(
            cartId = "CART-100",
            userId = "USR-001",
            productId = "PRD-SNK-001",
            quantity = 2,
            requestedUnitPrice = 1,
            sourcePage = "shop"
        )

        assertThat(item.productName).isEqualTo("Daily Runner Sneakers")
        assertThat(item.unitPrice).isEqualTo(89000)
        assertThat(item.lineTotal).isEqualTo(178000)
        assertThat(item.availableQuantity).isEqualTo(12)
    }

    @Test
    fun `addItem - 품절 상품은 담을 수 없음`() {
        assertThatThrownBy {
            cartService.addItem(
                cartId = "CART-100",
                userId = "USR-001",
                productId = "PRD-ELC-042",
                quantity = 1,
                requestedUnitPrice = 159000,
                sourcePage = "shop"
            )
        }
            .isInstanceOf(ResponseStatusException::class.java)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `addItem - 재고보다 많은 수량은 담을 수 없음`() {
        assertThatThrownBy {
            cartService.addItem(
                cartId = "CART-100",
                userId = "USR-001",
                productId = "PRD-SNK-001",
                quantity = 99,
                requestedUnitPrice = 89000,
                sourcePage = "shop"
            )
        }
            .isInstanceOf(ResponseStatusException::class.java)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.CONFLICT)
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
        ): List<ProductResponse> = products

        override fun findByProductId(productId: String): ProductResponse? {
            return products.firstOrNull { it.productId == productId }
        }
    }
}
