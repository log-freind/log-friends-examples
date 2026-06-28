package com.example.demo.catalog

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CatalogController::class)
class CatalogControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var catalogService: CatalogService

    @Test
    fun `GET products - 상품 목록을 반환`() {
        given(catalogService.listProducts(null, null, null, null, null)).willReturn(
            listOf(
                ProductResponse(
                    productId = "PRD-SNK-001",
                    name = "Daily Runner Sneakers",
                    category = "shoes",
                    price = 89000,
                    stockStatus = "IN_STOCK",
                    brand = "Northline",
                    rating = 4.7
                )
            )
        )

        mockMvc.perform(get("/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].productId").value("PRD-SNK-001"))
            .andExpect(jsonPath("$[0].category").value("shoes"))
            .andExpect(jsonPath("$[0].price").value(89000))
            .andExpect(jsonPath("$[0].stockStatus").value("IN_STOCK"))
    }

    @Test
    fun `GET products - category와 stockStatus 필터를 서비스에 전달`() {
        given(catalogService.listProducts(null, "bags", "LOW_STOCK", null, null)).willReturn(
            listOf(
                ProductResponse(
                    productId = "PRD-BAG-014",
                    name = "City Commuter Backpack",
                    category = "bags",
                    price = 129000,
                    stockStatus = "LOW_STOCK",
                    brand = "UrbanTrail",
                    rating = 4.5
                )
            )
        )

        mockMvc.perform(
            get("/products")
                .param("category", "bags")
                .param("stockStatus", "LOW_STOCK")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].productId").value("PRD-BAG-014"))
            .andExpect(jsonPath("$[0].stockStatus").value("LOW_STOCK"))
    }

    @Test
    fun `GET products - 검색어와 가격 필터를 서비스에 전달`() {
        given(catalogService.listProducts("runner", "shoes", "IN_STOCK", 50000, 100000)).willReturn(
            listOf(
                ProductResponse(
                    productId = "PRD-SNK-001",
                    name = "Daily Runner Sneakers",
                    category = "shoes",
                    price = 89000,
                    stockStatus = "IN_STOCK",
                    brand = "Northline",
                    rating = 4.7
                )
            )
        )

        mockMvc.perform(
            get("/products")
                .param("q", "runner")
                .param("category", "shoes")
                .param("stockStatus", "IN_STOCK")
                .param("minPrice", "50000")
                .param("maxPrice", "100000")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].productId").value("PRD-SNK-001"))
    }

    @Test
    fun `GET products productId - 상품 상세를 반환`() {
        given(catalogService.getProduct("PRD-SNK-001")).willReturn(
            ProductResponse(
                productId = "PRD-SNK-001",
                name = "Daily Runner Sneakers",
                category = "shoes",
                price = 89000,
                stockStatus = "IN_STOCK",
                brand = "Northline",
                rating = 4.7
            )
        )

        mockMvc.perform(get("/products/PRD-SNK-001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.productId").value("PRD-SNK-001"))
            .andExpect(jsonPath("$.name").value("Daily Runner Sneakers"))
            .andExpect(jsonPath("$.category").value("shoes"))
            .andExpect(jsonPath("$.price").value(89000))
            .andExpect(jsonPath("$.stockStatus").value("IN_STOCK"))
    }

    @Test
    fun `GET products productId - 없는 상품이면 404 반환`() {
        given(catalogService.getProduct("PRD-NOT-FOUND")).willReturn(null)

        mockMvc.perform(get("/products/PRD-NOT-FOUND"))
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))
    }
}
