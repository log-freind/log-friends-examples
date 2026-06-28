package com.example.demo.cart

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(CartController::class)
class CartControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var cartService: CartService

    @Test
    fun `POST carts items - 유효한 요청은 200과 cart item 반환`() {
        given(
            cartService.addItem(
                cartId = "CART-100",
                userId = "USR-001",
                productId = "PROD-SSD-1TB",
                quantity = 2,
                requestedUnitPrice = 129000,
                sourcePage = "product-detail"
            )
        ).willReturn(
            CartItemResponse(
                cartId = "CART-100",
                userId = "USR-001",
                productId = "PROD-SSD-1TB",
                productName = "SSD 1TB",
                quantity = 2,
                unitPrice = 129000,
                lineTotal = 258000,
                sourcePage = "product-detail",
                stockStatus = "IN_STOCK",
                availableQuantity = 12
            )
        )

        mockMvc.perform(
            post("/carts/CART-100/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "USR-001",
                      "productId": "PROD-SSD-1TB",
                      "quantity": 2,
                      "unitPrice": 129000,
                      "sourcePage": "product-detail"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cartId").value("CART-100"))
            .andExpect(jsonPath("$.userId").value("USR-001"))
            .andExpect(jsonPath("$.productId").value("PROD-SSD-1TB"))
            .andExpect(jsonPath("$.productName").value("SSD 1TB"))
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.unitPrice").value(129000))
            .andExpect(jsonPath("$.lineTotal").value(258000))
            .andExpect(jsonPath("$.sourcePage").value("product-detail"))
            .andExpect(jsonPath("$.stockStatus").value("IN_STOCK"))
            .andExpect(jsonPath("$.availableQuantity").value(12))
    }

    @Test
    fun `POST carts items - productId 누락 시 400 반환`() {
        mockMvc.perform(
            post("/carts/CART-100/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"USR-001","quantity":2,"unitPrice":129000}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `DELETE carts items - userId와 sourcePage가 있으면 204 반환`() {
        willDoNothing()
            .given(cartService)
            .removeItem(
                cartId = "CART-100",
                userId = "USR-001",
                productId = "PROD-SSD-1TB",
                sourcePage = "cart-page"
            )

        mockMvc.perform(
            delete("/carts/CART-100/items/PROD-SSD-1TB")
                .param("userId", "USR-001")
                .param("sourcePage", "cart-page")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `DELETE carts items - userId 누락 시 400 반환`() {
        mockMvc.perform(
            delete("/carts/CART-100/items/PROD-SSD-1TB")
        )
            .andExpect(status().isBadRequest)
    }
}
