package com.example.demo.shop

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ShopPageController::class)
class ShopPageControllerTest {

    @Autowired lateinit var mockMvc: MockMvc

    @Test
    fun `GET root - 쇼핑몰 화면으로 진입`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(forwardedUrl("/shop.html"))
    }

    @Test
    fun `GET shop - 쇼핑몰 화면으로 진입`() {
        mockMvc.perform(get("/shop"))
            .andExpect(status().isOk)
            .andExpect(forwardedUrl("/shop.html"))
    }
}
