package com.example.demo.user

import com.logfriends.agent.annotation.LogCategory
import com.logfriends.agent.annotation.LogLevel
import com.logfriends.agent.spec.LogSpec
import com.logfriends.agent.spec.LogSpecDef
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserLogConfig {

    @Bean
    fun userRegisteredSpec(): LogSpecDef {
        return LogSpec.define("user.registered")
            .description("신규 사용자 가입")
            .level(LogLevel.INFO).category(LogCategory.AUDIT)
            .field("name").type(String::class.java).required().example("홍길동").and()
            .field("email").type(String::class.java).required().example("hong@example.com").and()
            .build()
    }

    @Bean
    fun userDeactivatedSpec(): LogSpecDef {
        return LogSpec.define("user.deactivated")
            .description("사용자 계정 비활성화")
            .level(LogLevel.WARN).category(LogCategory.AUDIT)
            .field("userId").type(String::class.java).required().example("USR-1002").and()
            .build()
    }
}
