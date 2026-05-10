package ru.radeflex.substest.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class ProductCreateDto(
    @field:Size(min = 3, max = 100, message = "Name size must be between 3 and 10")
    val name: String,
    @field:Min(1, message = "Price must be positive")
    val price: Int,
)