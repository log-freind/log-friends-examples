package com.example.demo.payment

import com.logfriends.agent.annotation.LogCategory
import com.logfriends.agent.annotation.LogLevel
import com.logfriends.agent.spec.LogSpec
import com.logfriends.agent.spec.LogSpecDef
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaymentLogConfig {

    @Bean
    fun paymentProcessedSpec(): LogSpecDef {
        return LogSpec.define("payment.processed")
            .description("결제 처리 완료")
            .level(LogLevel.INFO).category(LogCategory.BUSINESS)
            .field("orderId").type(String::class.java).required().example("ORD-162334").and()
            .field("amount").type(java.lang.Integer::class.java).required().example("50000").and()
            .field("method").type(String::class.java).required().example("CREDIT_CARD").and()
            .build()
    }

    @Bean
    fun paymentRefundedSpec(): LogSpecDef {
        return LogSpec.define("payment.refunded")
            .description("결제 환불 완료")
            .level(LogLevel.WARN).category(LogCategory.BUSINESS)
            .field("txId").type(String::class.java).required().example("TX-998877").and()
            .field("reason").type(String::class.java).required().example("주문취소").and()
            .build()
    }
}
