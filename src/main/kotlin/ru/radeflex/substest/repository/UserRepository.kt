package ru.radeflex.substest.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.radeflex.substest.entity.User

interface UserRepository : JpaRepository<User, Int> {
    fun existsByUsername(username: String): Boolean
}