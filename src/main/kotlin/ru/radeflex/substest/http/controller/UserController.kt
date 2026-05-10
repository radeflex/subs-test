package ru.radeflex.substest.http.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.radeflex.substest.dto.UserReadDto
import ru.radeflex.substest.service.UserService

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun create(username: String): ResponseEntity<UserReadDto> {
        return ResponseEntity.ok(userService.create(username))
    }
}