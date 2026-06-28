package com.example.demo.cart

import com.logfriends.agent.annotation.LogField

data class CartItemRequest(
    @field:LogField(description = "User identifier that owns the shopping cart", type = "STRING")
    val userId: String,

    @field:LogField(description = "Product identifier selected by the customer", type = "STRING")
    val productId: String,

    @field:LogField(description = "Number of products added to the cart", type = "INT")
    val quantity: Int,

    @field:LogField(description = "Client-observed unit price. Server recalculates from catalog", type = "INT", required = false)
    val unitPrice: Int? = null,

    @field:LogField(description = "Page or placement where the add-to-cart action started", type = "STRING", required = false)
    val sourcePage: String = "product-detail"
)
