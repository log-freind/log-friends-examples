package com.example.demo.coupon

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/coupons")
class CouponController(private val couponService: CouponService) {

    @PostMapping("/validate")
    fun validate(@RequestBody request: CouponValidationRequest): ResponseEntity<CouponValidationResponse> {
        val result = couponService.validate(
            userId = request.userId,
            couponCode = request.couponCode,
            orderTotal = request.orderTotal
        )
        return ResponseEntity.ok(result)
    }
}
