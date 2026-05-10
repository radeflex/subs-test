package ru.radeflex.substest.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.Instant

@Entity
class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @ManyToOne
    var user: User? = null,
    @ManyToOne
    var product: Product? = null,
    @Enumerated(EnumType.STRING)
    var status: SubscriptionStatus? = null,
    val createdAt: Instant = Instant.now(),
    var expiresAt: Instant? = null,
    var pausedAt: Instant? = null
) {
}