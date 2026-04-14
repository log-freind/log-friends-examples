package com.example.demo.order

import com.logfriends.agent.annotation.LogEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderService {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    @LogEvent("order.created")
    fun create(productId: String, quantity: Int, userId: String): String {
        log.info("Creating order for product {}, user {}", productId, userId)
        return "ORD-" + System.currentTimeMillis()
    }

    @LogEvent("order.cancelled")
    fun cancel(orderId: String, reason: String) {
        log.warn("Canceling order {}, reason: {}", orderId, reason)
    }
}
