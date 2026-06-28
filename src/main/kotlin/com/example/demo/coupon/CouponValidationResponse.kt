package com.example.demo.coupon

data class CouponValidationResponse(
    val couponCode: String,
    val valid: Boolean,
    val discountAmount: Int,
    val finalAmount: Int,
    val reason: String
)
