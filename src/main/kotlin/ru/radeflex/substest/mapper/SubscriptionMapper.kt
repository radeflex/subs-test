package ru.radeflex.substest.mapper

import org.springframework.stereotype.Component
import ru.radeflex.substest.dto.SubscriptionCreateDto
import ru.radeflex.substest.dto.SubscriptionReadDto
import ru.radeflex.substest.entity.Product
import ru.radeflex.substest.entity.Subscription
import ru.radeflex.substest.entity.SubscriptionStatus
import ru.radeflex.substest.repository.UserRepository

@Component
class SubscriptionMapper(
    private val userRepository: UserRepository,
) {
    fun map(sub: Subscription): SubscriptionReadDto {
        return SubscriptionReadDto(
            userId = sub.user?.id,
            productId = sub.product?.id,
            status = sub.status,
            expiresAt = sub.expiresAt
        )
    }
    fun map(sub: SubscriptionCreateDto, s: Product): Subscription {
        val u = userRepository.findById(sub.userId).orElseThrow()
        return Subscription(
            user = u,
            product = s,
            status = SubscriptionStatus.ACTIVE,
            expiresAt = sub.expiresAt)
    }
}
