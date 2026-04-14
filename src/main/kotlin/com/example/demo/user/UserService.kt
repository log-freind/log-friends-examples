package com.example.demo.user

import com.logfriends.agent.annotation.LogEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    @LogEvent("user.registered")
    fun register(name: String, email: String): String {
        log.info("Registering new user: {} ({})", name, email)
        return "USR-" + System.currentTimeMillis()
    }

    @LogEvent("user.deactivated")
    fun deactivate(userId: String) {
        log.warn("Deactivating user {}", userId)
    }
}
