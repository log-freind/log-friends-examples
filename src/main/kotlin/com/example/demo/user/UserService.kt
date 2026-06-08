package com.example.demo.user

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import com.logfriends.agent.annotation.LogMasked
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    @LogEvent(
        name = "userRegistered",
        description = "User registration business eventName",
        apiMethod = "POST",
        apiPath = "/users",
        apiDescription = "Registers a new example user"
    )
    fun register(
        @LogField(description = "Registered user display name", type = "STRING")
        name: String,
        @LogMasked
        @LogField(description = "Registered user email. SDK sends __MASKED__", type = "STRING")
        email: String
    ): String {
        log.info("Registering new user {}", name)
        return "USR-" + System.currentTimeMillis()
    }

    @LogEvent(
        name = "userDeactivated",
        description = "User deactivation business eventName",
        apiMethod = "PUT",
        apiPath = "/users/{userId}/deactivate",
        apiDescription = "Deactivates an example user account"
    )
    fun deactivate(
        @LogField(description = "Deactivated user identifier", type = "STRING")
        userId: String
    ) {
        log.warn("Deactivating user {}", userId)
    }
}
