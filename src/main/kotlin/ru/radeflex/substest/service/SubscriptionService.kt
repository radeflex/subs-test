package ru.radeflex.substest.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.radeflex.substest.dto.SubscriptionCreateDto
import ru.radeflex.substest.dto.SubscriptionFilter
import ru.radeflex.substest.dto.SubscriptionReadDto
import ru.radeflex.substest.entity.Subscription
import ru.radeflex.substest.entity.SubscriptionStatus
import ru.radeflex.substest.mapper.SubscriptionMapper
import ru.radeflex.substest.repository.ProductRepository
import ru.radeflex.substest.repository.SubscriptionRepository
import ru.radeflex.substest.specs.SubscriptionSpecs
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class SubscriptionService(
    private val subscriptionMapper: SubscriptionMapper,
    private val subscriptionRepository: SubscriptionRepository,
    private val productRepository: ProductRepository
) {
    fun findAll(filter: SubscriptionFilter, pageable: Pageable): Page<SubscriptionReadDto> {
        return subscriptionRepository.findAll(build(filter), pageable)
            .map { subscriptionMapper.map(it) }
    }

    fun findById(id: Int): Optional<SubscriptionReadDto> {
        return subscriptionRepository.findById(id)
            .map { subscriptionMapper.map(it) }
    }

    fun findAllActive(userId: Int, pageable: Pageable): Page<SubscriptionReadDto> {
        return subscriptionRepository.findAllByUserIdAndStatus(
            userId, SubscriptionStatus.ACTIVE, pageable)
            .map { subscriptionMapper.map(it) }
    }

    private fun build(filter: SubscriptionFilter): Specification<Subscription> {
        return Specification
            .where(SubscriptionSpecs.status(filter.status))
            .and(SubscriptionSpecs.userId(filter.userId))
            .and(SubscriptionSpecs.expiresAtBefore(filter.expiresAtBefore))
            .and(SubscriptionSpecs.productId(filter.productId))
    }

    @Transactional
    fun create(dto: SubscriptionCreateDto): Optional<SubscriptionReadDto> {
        if (subscriptionRepository.existsByUserIdAndProductId(dto.userId, dto.productId)) {
            throw IllegalArgumentException("subscription already exists")
        }
        return productRepository.findById(dto.productId)
            .map { s -> subscriptionMapper.map(dto, s ) }
            .map { subscriptionRepository.save(it)}
            .map { subscriptionMapper.map(it) }
    }

    @Transactional
    fun cancel(id: Int): Boolean {
        val sub = subscriptionRepository.findById(id)
        if (sub.isPresent) {
            sub.get().status = SubscriptionStatus.CANCELLED
            sub.get().expiresAt = null
            sub.get().pausedAt = null
            subscriptionRepository.save(sub.get())
            return true
        }
        return false
    }

    @Transactional
    fun pause(id: Int): Boolean {
        val sub = subscriptionRepository.findById(id).orElse(null)
            ?: return false
        sub.status = SubscriptionStatus.PAUSED
        sub.pausedAt = Instant.now()
        subscriptionRepository.save(sub)
        return true
    }

    @Transactional
    fun resume(id: Int, expiresAt: Instant?): Boolean {
        val sub = subscriptionRepository.findById(id).orElse(null)
            ?: return false
        val now = Instant.now()
        when (sub.status) {
            SubscriptionStatus.ACTIVE -> return false

            SubscriptionStatus.PAUSED -> {
                val pausedAt = sub.pausedAt ?: return false
                val pauseDuration = Duration.between(pausedAt, now)
                sub.expiresAt = sub.expiresAt?.plus(pauseDuration)
                sub.pausedAt = null
                sub.status = SubscriptionStatus.ACTIVE
            }

            SubscriptionStatus.CANCELLED,
            SubscriptionStatus.EXPIRED -> {
                if (expiresAt == null ||
                    expiresAt.isBefore(Instant.now())) return false

                sub.expiresAt = expiresAt
                sub.status = SubscriptionStatus.ACTIVE
            }

            else -> return false
        }

        subscriptionRepository.save(sub)
        return true
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun expire() {
        subscriptionRepository.expireAll(Instant.now())
    }
}