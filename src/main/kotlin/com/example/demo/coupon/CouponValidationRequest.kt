package com.example.demo.coupon

import com.logfriends.agent.annotation.LogField

data class CouponValidationRequest(
    @field:LogField(description = "User identifier that tries to apply the coupon", type = "STRING")
    val userId: String,

    @field:LogField(description = "Coupon code entered by the customer", type = "STRING")
    val couponCode: String,

    @field:LogField(description = "Order total before coupon discount", type = "INT")
    val orderTotal: Int
)
