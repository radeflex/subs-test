package ru.radeflex.substest.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.radeflex.substest.entity.Product

interface ProductRepository : JpaRepository<Product, Int> {
    fun existsByName(name: String): Boolean
}