package com.example.demo.order

import com.logfriends.agent.annotation.LogEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderAuditRepository: OrderAuditRepository
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    @LogEvent("orderCreated")
    fun create(request: OrderRequest): String {
        log.info("Creating order for product {}, user {}", request.productId, request.userId)
        val orderId = "ORD-" + System.currentTimeMillis()
        orderAuditRepository.recordCreated(orderId, request.productId, request.quantity, request.userId)
        return orderId
    }

    @LogEvent("orderCancelled")
    fun cancel(orderId: String, reason: String) {
        log.warn("Canceling order {}, reason: {}", orderId, reason)
    }
}
