package com.example.demo.order

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean  lateinit var orderService: OrderService

    @Test
    fun `POST orders - 유효한 요청은 200과 orderId 반환`() {
        given(orderService.create("PROD-1", 2, "USR-001")).willReturn("ORD-123")

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"productId":"PROD-1","quantity":2,"userId":"USR-001"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("ORD-123"))
    }

    @Test
    fun `POST orders - 필드 누락 시 400 반환`() {
        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"productId":"PROD-1"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `DELETE orders - reason 있으면 204 반환`() {
        willDoNothing().given(orderService).cancel("ORD-123", "changed mind")

        mockMvc.perform(
            delete("/orders/ORD-123")
                .param("reason", "changed mind")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `POST orders - 수량 1이상이면 정상 처리`() {
        given(orderService.create("PROD-2", 1, "USR-002")).willReturn("ORD-456")

        mockMvc.perform(
            post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"productId":"PROD-2","quantity":1,"userId":"USR-002"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("ORD-456"))
    }
}
