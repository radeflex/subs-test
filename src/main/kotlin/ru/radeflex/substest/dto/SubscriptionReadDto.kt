package ru.radeflex.substest.dto

import ru.radeflex.substest.entity.SubscriptionStatus
import java.time.Instant

data class SubscriptionReadDto(
    val userId: Int?,
    val productId: Int?,
    val status: SubscriptionStatus?,
    val expiresAt: Instant?
)
