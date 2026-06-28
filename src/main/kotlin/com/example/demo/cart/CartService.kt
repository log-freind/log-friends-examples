package com.example.demo.cart

import com.example.demo.catalog.ProductCatalogRepository
import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CartService(
    private val productCatalogRepository: ProductCatalogRepository
) {
    private val log = LoggerFactory.getLogger(CartService::class.java)

    @LogEvent(
        name = "cartItemAdded",
        description = "Cart item addition business eventName",
        apiMethod = "POST",
        apiPath = "/carts/{cartId}/items",
        apiDescription = "Adds a product to a customer's shopping cart"
    )
    fun addItem(
        @LogField(description = "Shopping cart identifier", type = "STRING")
        cartId: String,
        @LogField(description = "User identifier that owns the shopping cart", type = "STRING")
        userId: String,
        @LogField(description = "Product identifier selected by the customer", type = "STRING")
        productId: String,
        @LogField(description = "Number of products added to the cart", type = "INT")
        quantity: Int,
        @LogField(description = "Client-observed unit price. Server recalculates from catalog", type = "INT", required = false)
        requestedUnitPrice: Int?,
        @LogField(description = "Page or placement where the add-to-cart action started", type = "STRING")
        sourcePage: String
    ): CartItemResponse {
        val product = productCatalogRepository.findByProductId(productId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $productId")
        if (quantity <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero")
        }
        if (product.stockStatus == "SOLD_OUT" || product.stockQuantity <= 0) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Product is sold out: $productId")
        }
        if (quantity > product.stockQuantity) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only ${product.stockQuantity} items available")
        }

        log.info(
            "Adding product {} x{} to cart {} for user {} from {}. requestedUnitPrice={}, catalogUnitPrice={}",
            productId,
            quantity,
            cartId,
            userId,
            sourcePage,
            requestedUnitPrice,
            product.price
        )

        return CartItemResponse(
            cartId = cartId,
            userId = userId,
            productId = productId,
            productName = product.name,
            quantity = quantity,
            unitPrice = product.price,
            lineTotal = quantity * product.price,
            sourcePage = sourcePage,
            stockStatus = product.stockStatus,
            availableQuantity = product.stockQuantity
        )
    }

    @LogEvent(
        name = "cartItemRemoved",
        description = "Cart item removal business eventName",
        apiMethod = "DELETE",
        apiPath = "/carts/{cartId}/items/{productId}",
        apiDescription = "Removes a product from a customer's shopping cart"
    )
    fun removeItem(
        @LogField(description = "Shopping cart identifier", type = "STRING")
        cartId: String,
        @LogField(description = "User identifier that owns the shopping cart", type = "STRING")
        userId: String,
        @LogField(description = "Product identifier removed by the customer", type = "STRING")
        productId: String,
        @LogField(description = "Page or placement where the remove-from-cart action started", type = "STRING")
        sourcePage: String
    ) {
        log.info(
            "Removing product {} from cart {} for user {} from {}",
            productId,
            cartId,
            userId,
            sourcePage
        )
    }
}
