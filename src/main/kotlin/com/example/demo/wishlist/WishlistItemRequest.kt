package com.example.demo.wishlist

import com.logfriends.agent.annotation.LogField

data class WishlistItemRequest(
    @field:LogField(description = "User identifier that owns the wishlist", type = "STRING")
    val userId: String,

    @field:LogField(description = "Product identifier saved to the wishlist", type = "STRING")
    val productId: String,

    @field:LogField(description = "Page or placement where the wishlist action started", type = "STRING")
    val sourcePage: String = "product-card"
)
