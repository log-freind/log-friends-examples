package com.example.demo.order

import com.logfriends.agent.annotation.LogField

data class OrderLineItem(
    @field:LogField(description = "Product identifier in the order line", type = "STRING")
    val productId: String,

    @field:LogField(description = "Quantity ordered for this product", type = "INT")
    val quantity: Int,

    @field:LogField(description = "Catalog unit price at checkout time", type = "INT", required = false)
    val unitPrice: Int? = null,

    @field:LogField(description = "Line total at checkout time", type = "INT", required = false)
    val lineTotal: Int? = null
)
