package ru.radeflex.substest.dto

import jakarta.validation.constraints.Future
import java.time.Instant

data class SubscriptionCreateDto(
    val userId: Int,
    val productId: Int,
    @field:Future
    val expiresAt: Instant?,
)
