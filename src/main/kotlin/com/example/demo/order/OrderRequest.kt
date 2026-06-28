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
    val couponCode: String? = null,

    /** Sales channel where the customer placed the order. */
    @field:LogField(description = "Sales channel such as WEB, MOBILE_APP, or PARTNER_STORE", type = "STRING", required = false)
    val channel: String? = null,

    /** Expected customer payment amount after coupon and item quantity are applied. */
    @field:LogField(description = "Expected customer payment amount after discounts", type = "INT", required = false)
    val orderTotal: Int? = null,

    /** Delivery method selected by the customer. */
    @field:LogField(description = "Delivery method such as STANDARD, EXPRESS, or PICKUP", type = "STRING", required = false)
    val deliveryMethod: String? = null,

    /** Postal code used for delivery routing. */
    @field:LogField(description = "Destination postal code used for delivery routing", type = "STRING", required = false)
    val shippingZipCode: String? = null,

    /** Optional line items when checkout contains multiple products. */
    @field:LogField(description = "Line items included in a cart checkout", type = "JSON", required = false)
    val items: List<OrderLineItem>? = null
)
