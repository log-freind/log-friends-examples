package com.example.demo.order

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import

@JdbcTest
@Import(OrderAuditRepository::class)
class OrderAuditRepositoryTest {

    @Autowired
    lateinit var orderAuditRepository: OrderAuditRepository

    @Test
    fun `recordCreated stores a JDBC-backed audit row`() {
        orderAuditRepository.recordCreated(
            orderId = "ORD-1001",
            productId = "PROD-1",
            quantity = 2,
            userId = "USR-1"
        )

        assertThat(orderAuditRepository.countByOrderId("ORD-1001")).isEqualTo(1)
    }
}
