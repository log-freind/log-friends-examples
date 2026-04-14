package com.example.demo.payment

import com.logfriends.agent.annotation.LogEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentService {
    private val log = LoggerFactory.getLogger(PaymentService::class.java)

    @LogEvent("payment.processed")
    fun processPayment(orderId: String, amount: Int, method: String): String {
        log.info("Processing payment for order {} via {}", orderId, method)
        return "TX-" + System.currentTimeMillis()
    }

    @LogEvent("payment.refunded")
    fun refund(txId: String, reason: String) {
        log.info("Refunding transaction {}, reason: {}", txId, reason)
    }
}
