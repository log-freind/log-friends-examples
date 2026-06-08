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
        log.info("Creating order for product {}, user {}", request.productId, request.userId)
        val orderId = "ORD-" + System.currentTimeMillis()
        orderAuditRepository.recordCreated(orderId, request.productId, request.quantity, request.userId)
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
}
