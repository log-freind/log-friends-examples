package com.example.demo.order

import com.logfriends.agent.annotation.LogCategory
import com.logfriends.agent.annotation.LogLevel
import com.logfriends.agent.spec.LogSpec
import com.logfriends.agent.spec.LogSpecDef
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OrderLogConfig {

    @Bean
    fun orderCreatedSpec(): LogSpecDef {
        return LogSpec.define("order.created")
            .description("주문 생성 이벤트")
            .level(LogLevel.INFO).category(LogCategory.BUSINESS)
            .field("productId").type(String::class.java).required().example("PROD-001").and()
            .field("quantity").type(java.lang.Integer::class.java).required().example("2").and()
            .field("userId").type(String::class.java).required().example("user123").and()
            .build()
    }

    @Bean
    fun orderCancelledSpec(): LogSpecDef {
        return LogSpec.define("order.cancelled")
            .description("주문 취소 이벤트")
            .level(LogLevel.WARN).category(LogCategory.BUSINESS)
            .field("orderId").type(String::class.java).required().example("ORD-162334").and()
            .field("reason").type(String::class.java).required().example("단순변심").and()
            .build()
    }
}
