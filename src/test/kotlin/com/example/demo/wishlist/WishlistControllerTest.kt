package com.example.demo.wishlist

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(WishlistController::class)
class WishlistControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var wishlistService: WishlistService

    @Test
    fun `POST wishlists items - 상품을 위시리스트에 담는다`() {
        given(
            wishlistService.addItem(
                wishlistId = "WISH-100",
                userId = "USR-001",
                productId = "PRD-SNK-001",
                sourcePage = "product-card"
            )
        ).willReturn(
            WishlistItemResponse(
                wishlistId = "WISH-100",
                userId = "USR-001",
                productId = "PRD-SNK-001",
                sourcePage = "product-card"
            )
        )

        mockMvc.perform(
            post("/wishlists/WISH-100/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "USR-001",
                      "productId": "PRD-SNK-001",
                      "sourcePage": "product-card"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.wishlistId").value("WISH-100"))
            .andExpect(jsonPath("$.userId").value("USR-001"))
            .andExpect(jsonPath("$.productId").value("PRD-SNK-001"))
            .andExpect(jsonPath("$.sourcePage").value("product-card"))
    }

    @Test
    fun `DELETE wishlists items - 상품을 위시리스트에서 제거한다`() {
        willDoNothing()
            .given(wishlistService)
            .removeItem(
                wishlistId = "WISH-100",
                userId = "USR-001",
                productId = "PRD-SNK-001",
                sourcePage = "wishlist-page"
            )

        mockMvc.perform(
            delete("/wishlists/WISH-100/items/PRD-SNK-001")
                .param("userId", "USR-001")
                .param("sourcePage", "wishlist-page")
        )
            .andExpect(status().isNoContent)
    }
}
