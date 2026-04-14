package com.example.demo.payment

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

@WebMvcTest(PaymentController::class)
class PaymentControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean  lateinit var paymentService: PaymentService

    @Test
    fun `POST payments - 유효한 요청은 200과 txId 반환`() {
        given(paymentService.processPayment("ORD-1", 50000, "CARD")).willReturn("TX-001")

        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"ORD-1","amount":50000,"method":"CARD"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("TX-001"))
    }

    @Test
    fun `POST payments - 필드 누락 시 400 반환`() {
        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"ORD-1"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST payments refund - 204 반환`() {
        willDoNothing().given(paymentService).refund("TX-001", "defective")

        mockMvc.perform(
            post("/payments/TX-001/refund")
                .param("reason", "defective")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `POST payments - KAKAO_PAY 결제 수단도 처리`() {
        given(paymentService.processPayment("ORD-2", 10000, "KAKAO_PAY")).willReturn("TX-002")

        mockMvc.perform(
            post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"ORD-2","amount":10000,"method":"KAKAO_PAY"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("TX-002"))
    }
}
