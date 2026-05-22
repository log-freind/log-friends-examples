package com.example.demo.user

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogMasked
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    @LogEvent("userRegistered")
    fun register(name: String, @LogMasked email: String): String {
        log.info("Registering new user {}", name)
        return "USR-" + System.currentTimeMillis()
    }

    @LogEvent("userDeactivated")
    fun deactivate(userId: String) {
        log.warn("Deactivating user {}", userId)
    }
}
