package com.example.demo.order

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class OrderAuditRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun recordCreated(orderId: String, productId: String, quantity: Int, userId: String) {
        jdbcTemplate.update(
            """
            INSERT INTO order_audit (order_id, product_id, quantity, user_id)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            orderId,
            productId,
            quantity,
            userId
        )
    }

    fun countByOrderId(orderId: String): Int {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM order_audit WHERE order_id = ?",
            Int::class.java,
            orderId
        ) ?: 0
    }
}
