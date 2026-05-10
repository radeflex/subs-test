package ru.radeflex.substest.dto

import ru.radeflex.substest.entity.SubscriptionStatus
import java.time.Instant

data class SubscriptionFilter(
    val userId: Int? = null,
    val productId: Int? = null,
    val status: SubscriptionStatus? = null,
    val expiresAtBefore: Instant? = null,
)
