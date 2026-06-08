package com.example.demo.payment

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentService {
    private val log = LoggerFactory.getLogger(PaymentService::class.java)

    @LogEvent(
        name = "paymentProcessed",
        description = "Payment processed business eventName",
        apiMethod = "POST",
        apiPath = "/payments",
        apiDescription = "Processes a payment for an order"
    )
    fun processPayment(
        @LogField(description = "Order identifier connected to the payment", type = "STRING")
        orderId: String,
        @LogField(description = "Payment amount in the example request", type = "INT")
        amount: Int,
        @LogField(description = "Payment method such as CARD", type = "STRING")
        method: String
    ): String {
        log.info("Processing payment for order {} via {}", orderId, method)
        return "TX-" + System.currentTimeMillis()
    }

    @LogEvent(
        name = "paymentRefunded",
        description = "Payment refund business eventName",
        apiMethod = "POST",
        apiPath = "/payments/{transactionId}/refund",
        apiDescription = "Refunds an existing payment transaction"
    )
    fun refund(
        @LogField(description = "Refunded payment transaction identifier", type = "STRING")
        txId: String,
        @LogField(description = "Refund reason", type = "STRING")
        reason: String
    ) {
        log.info("Refunding transaction {}, reason: {}", txId, reason)
    }
}
