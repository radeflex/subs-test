package ru.radeflex.substest.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.radeflex.substest.entity.Subscription
import ru.radeflex.substest.entity.SubscriptionStatus
import java.time.Instant

interface SubscriptionRepository : JpaRepository<Subscription, Int>,
    JpaSpecificationExecutor<Subscription> {
    @Modifying
    @Query("""
    UPDATE Subscription s
    SET s.status = 'EXPIRED'
    WHERE s.status = 'ACTIVE'
    AND s.expiresAt < :now
""")
    fun expireAll( now: Instant): Int
    fun findAllByUserIdAndStatus(userId: Int,
                                 status: SubscriptionStatus,
                                 pageable: Pageable): Page<Subscription>

    fun existsByUserIdAndProductId(userId: Int, productId: Int): Boolean
}