package com.example.demo.coupon

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class CouponService {
    private val log = LoggerFactory.getLogger(CouponService::class.java)

    @LogEvent(
        name = "couponValidated",
        description = "Coupon validation business eventName",
        apiMethod = "POST",
        apiPath = "/coupons/validate",
        apiDescription = "Validates a customer coupon code before checkout"
    )
    fun validate(
        @LogField(description = "User identifier that tries to apply the coupon", type = "STRING")
        userId: String,
        @LogField(description = "Coupon code entered by the customer", type = "STRING")
        couponCode: String,
        @LogField(description = "Order total before coupon discount", type = "INT")
        orderTotal: Int
    ): CouponValidationResponse {
        val normalizedCode = couponCode.trim().uppercase()
        val discountAmount = when (normalizedCode) {
            "WELCOME10" -> min(orderTotal / 10, 20_000)
            "FREESHIP" -> min(orderTotal, 3_000)
            else -> 0
        }
        val valid = discountAmount > 0
        val finalAmount = (orderTotal - discountAmount).coerceAtLeast(0)
        val reason = if (valid) "APPLIED" else "NOT_ELIGIBLE"

        log.info("Validating coupon {} for user {}, valid: {}", normalizedCode, userId, valid)
        return CouponValidationResponse(
            couponCode = normalizedCode,
            valid = valid,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            reason = reason
        )
    }
}
