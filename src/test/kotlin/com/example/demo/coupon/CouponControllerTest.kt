package com.example.demo.coupon

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CouponController::class)
class CouponControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var couponService: CouponService

    @Test
    fun `POST coupons validate - 쿠폰 검증 결과를 반환한다`() {
        given(couponService.validate("USR-001", "WELCOME10", 120000)).willReturn(
            CouponValidationResponse(
                couponCode = "WELCOME10",
                valid = true,
                discountAmount = 12000,
                finalAmount = 108000,
                reason = "APPLIED"
            )
        )

        mockMvc.perform(
            post("/coupons/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": "USR-001",
                      "couponCode": "WELCOME10",
                      "orderTotal": 120000
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.couponCode").value("WELCOME10"))
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.discountAmount").value(12000))
            .andExpect(jsonPath("$.finalAmount").value(108000))
            .andExpect(jsonPath("$.reason").value("APPLIED"))
    }
}
