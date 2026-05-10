package ru.radeflex.substest.service

import org.springframework.stereotype.Service
import ru.radeflex.substest.dto.UserReadDto
import ru.radeflex.substest.entity.User
import ru.radeflex.substest.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun create(username: String): UserReadDto? {
        if (userRepository.existsByUsername(username))
            throw IllegalArgumentException("User $username already exists")
        val u = userRepository.save(User(username = username))
        return UserReadDto(u.id, u.username)
    }
}