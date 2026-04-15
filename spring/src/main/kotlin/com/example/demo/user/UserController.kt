package com.example.demo.user

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun register(@RequestBody request: UserRequest): ResponseEntity<String> {
        val userId = userService.register(request.name, request.email)
        return ResponseEntity.ok(userId)
    }

    @PutMapping("/{userId}/deactivate")
    fun deactivate(@PathVariable userId: String): ResponseEntity<Void> {
        userService.deactivate(userId)
        return ResponseEntity.noContent().build()
    }
}
