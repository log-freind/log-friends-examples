package com.example.demo.catalog

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import

@JdbcTest
@Import(JdbcProductCatalogRepository::class)
class JdbcProductCatalogRepositoryTest {

    @Autowired
    lateinit var productCatalogRepository: ProductCatalogRepository

    @Test
    fun `seed data contains a realistic shopping mall catalog`() {
        val products = productCatalogRepository.findProducts(
            query = null,
            category = null,
            stockStatus = null,
            minPrice = null,
            maxPrice = null
        )

        assertThat(products).hasSize(60)
        assertThat(products).extracting("productId")
            .contains("PRD-SNK-001", "PRD-BAG-014", "PRD-ELC-042", "PRD-HOM-033")
        assertThat(products.map { it.category }.toSet())
            .containsExactlyInAnyOrder("shoes", "bags", "electronics", "outerwear", "home")
        assertThat(products).allSatisfy { product ->
            assertThat(product.price).isPositive()
            assertThat(product.stockStatus).isIn("IN_STOCK", "LOW_STOCK", "SOLD_OUT")
            if (product.stockStatus == "SOLD_OUT") {
                assertThat(product.stockQuantity).isZero()
            } else {
                assertThat(product.stockQuantity).isPositive()
            }
        }
    }

    @Test
    fun `seed data supports catalog filters`() {
        val shoes = productCatalogRepository.findProducts(
            query = null,
            category = "shoes",
            stockStatus = "IN_STOCK",
            minPrice = 50_000,
            maxPrice = 150_000
        )

        assertThat(shoes).isNotEmpty
        assertThat(shoes).allSatisfy { product ->
            assertThat(product.category).isEqualTo("shoes")
            assertThat(product.stockStatus).isEqualTo("IN_STOCK")
            assertThat(product.price).isBetween(50_000, 150_000)
        }
    }
}
