package com.example.demo.wishlist

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WishlistService {
    private val log = LoggerFactory.getLogger(WishlistService::class.java)

    @LogEvent(
        name = "wishlistItemAdded",
        description = "Wishlist item addition business eventName",
        apiMethod = "POST",
        apiPath = "/wishlists/{wishlistId}/items",
        apiDescription = "Adds a product to a customer's wishlist"
    )
    fun addItem(
        @LogField(description = "Wishlist identifier", type = "STRING")
        wishlistId: String,
        @LogField(description = "User identifier that owns the wishlist", type = "STRING")
        userId: String,
        @LogField(description = "Product identifier saved to the wishlist", type = "STRING")
        productId: String,
        @LogField(description = "Page or placement where the wishlist action started", type = "STRING")
        sourcePage: String
    ): WishlistItemResponse {
        log.info("Adding product {} to wishlist {} for user {} from {}", productId, wishlistId, userId, sourcePage)
        return WishlistItemResponse(
            wishlistId = wishlistId,
            userId = userId,
            productId = productId,
            sourcePage = sourcePage
        )
    }

    @LogEvent(
        name = "wishlistItemRemoved",
        description = "Wishlist item removal business eventName",
        apiMethod = "DELETE",
        apiPath = "/wishlists/{wishlistId}/items/{productId}",
        apiDescription = "Removes a product from a customer's wishlist"
    )
    fun removeItem(
        @LogField(description = "Wishlist identifier", type = "STRING")
        wishlistId: String,
        @LogField(description = "User identifier that owns the wishlist", type = "STRING")
        userId: String,
        @LogField(description = "Product identifier removed from the wishlist", type = "STRING")
        productId: String,
        @LogField(description = "Page or placement where the wishlist removal started", type = "STRING")
        sourcePage: String
    ) {
        log.info("Removing product {} from wishlist {} for user {} from {}", productId, wishlistId, userId, sourcePage)
    }
}
