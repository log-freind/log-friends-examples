package com.example.demo.order

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderAuditRepository: OrderAuditRepository
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    @LogEvent(
        name = "orderCreated",
        description = "Order creation business eventName",
        apiMethod = "POST",
        apiPath = "/orders",
        apiDescription = "Creates an order from an OrderRequest DTO"
    )
    fun create(
        @LogField(description = "OrderRequest DTO object", type = "JSON")
        request: OrderRequest
    ): String {
        val lineItems = request.items.orEmpty().ifEmpty {
            listOf(OrderLineItem(productId = request.productId, quantity = request.quantity))
        }
        log.info(
            "Creating {} order for user {}, items {}, delivery {}, total {}",
            request.channel ?: "WEB",
            request.userId,
            lineItems.size,
            request.deliveryMethod ?: "STANDARD",
            request.orderTotal ?: lineItems.sumOf { (it.lineTotal ?: 0) }
        )
        val orderId = "ORD-" + System.currentTimeMillis()
        lineItems.forEach { item ->
            orderAuditRepository.recordCreated(orderId, item.productId, item.quantity, request.userId)
        }
        return orderId
    }

    @LogEvent(
        name = "orderCancelled",
        description = "Order cancellation business eventName",
        apiMethod = "DELETE",
        apiPath = "/orders/{orderId}",
        apiDescription = "Cancels an existing order with a reason"
    )
    fun cancel(
        @LogField(description = "Cancelled order identifier", type = "STRING")
        orderId: String,
        @LogField(description = "Human-readable cancellation reason", type = "STRING")
        reason: String
    ) {
        log.warn("Canceling order {}, reason: {}", orderId, reason)
    }

    @LogEvent(
        name = "returnRequested",
        description = "Customer return request business eventName",
        apiMethod = "POST",
        apiPath = "/orders/{orderId}/return-requests",
        apiDescription = "Creates a return request for an order item"
    )
    fun requestReturn(
        @LogField(description = "Order identifier connected to the return request", type = "STRING")
        orderId: String,
        @LogField(description = "ReturnRequest DTO object", type = "JSON")
        request: ReturnRequest
    ): String {
        log.info("Requesting return for order {}, product {}, reason {}", orderId, request.productId, request.reason)
        return "RTN-" + System.currentTimeMillis()
    }
}
