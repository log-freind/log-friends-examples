package com.example.demo.order

import com.logfriends.agent.annotation.LogField

data class ReturnRequest(
    @field:LogField(description = "User identifier requesting the return", type = "STRING")
    val userId: String,

    @field:LogField(description = "Product identifier being returned", type = "STRING")
    val productId: String,

    @field:LogField(description = "Return reason selected by the customer", type = "STRING")
    val reason: String,

    @field:LogField(description = "Whether the package has already been opened", type = "BOOLEAN", required = false)
    val opened: Boolean = false
)
