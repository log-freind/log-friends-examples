package com.example.demo.order

import com.logfriends.agent.annotation.LogMasked
import com.logfriends.agent.annotation.LogField

/**
 * Order creation request used to demonstrate DTO payload capture.
 *
 * SDK keeps this DTO as one top-level LOG_EVENT payload field named `request`.
 * Top-level DTO fields can still be masked before transport.
 */
data class OrderRequest(
    /** Product identifier selected by the user. */
    @field:LogField(description = "Product identifier selected by the user", type = "STRING")
    val productId: String,

    /** Number of products to order. */
    @field:LogField(description = "Number of products to order", type = "INT")
    val quantity: Int,

    /** Example user identifier connected to the order. */
    @field:LogField(description = "Example user identifier connected to the order", type = "STRING")
    val userId: String,

    /** Buyer email. Masked by SDK before transport. */
    @field:LogMasked
    @field:LogField(description = "Buyer email. Masked by SDK before transport", type = "STRING", required = false)
    val customerEmail: String? = null,

    /** Optional coupon code applied to the order. */
    @field:LogField(description = "Optional coupon code applied to the order", type = "STRING", required = false)
    val couponCode: String? = null
)
