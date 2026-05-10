package ru.radeflex.substest.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import ru.radeflex.substest.dto.SubscriptionCreateDto
import ru.radeflex.substest.dto.SubscriptionFilter
import ru.radeflex.substest.dto.SubscriptionReadDto
import ru.radeflex.substest.entity.Product
import ru.radeflex.substest.entity.Subscription
import ru.radeflex.substest.entity.SubscriptionStatus
import ru.radeflex.substest.entity.User
import ru.radeflex.substest.mapper.SubscriptionMapper
import ru.radeflex.substest.repository.ProductRepository
import ru.radeflex.substest.repository.SubscriptionRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockitoExtension::class)
class SubscriptionServiceTest {

    @Mock
    private lateinit var subscriptionMapper: SubscriptionMapper

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @InjectMocks
    private lateinit var subscriptionService: SubscriptionService

    private lateinit var subscription: Subscription
    private lateinit var readDto: SubscriptionReadDto
    private lateinit var product: Product
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        product = Product(id = 10, name = "Premium")
        user = User(id = 10, username = "TestUser")
        subscription = Subscription(
            id = 1,
            user = user,
            product = product,
            status = SubscriptionStatus.ACTIVE,
            expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
        )

        readDto = SubscriptionReadDto(
            userId = 42,
            productId = 10,
            status = SubscriptionStatus.ACTIVE,
            expiresAt = subscription.expiresAt
        )
    }

    @Test
    fun `findAll returns mapped page`() {
        val filter = SubscriptionFilter(status = SubscriptionStatus.ACTIVE)
        val pageable = Pageable.ofSize(10)
        val page = PageImpl(listOf(subscription))

        `when`(subscriptionRepository.findAll(any<Specification<Subscription>>(), eq(pageable)))
            .thenReturn(page)
        `when`(subscriptionMapper.map(subscription)).thenReturn(readDto)

        val result = subscriptionService.findAll(filter, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(readDto, result.content.first())
    }

    @Test
    fun `findAll returns empty page when no subscriptions match`() {
        val filter = SubscriptionFilter()
        val pageable = Pageable.ofSize(10)

        `when`(subscriptionRepository.findAll(any<Specification<Subscription>>(), eq(pageable)))
            .thenReturn(PageImpl(emptyList()))

        val result = subscriptionService.findAll(filter, pageable)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `findById returns dto when subscription exists`() {
        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))
        `when`(subscriptionMapper.map(subscription)).thenReturn(readDto)

        val result = subscriptionService.findById(1)

        assertTrue(result.isPresent)
        assertEquals(readDto, result.get())
    }

    @Test
    fun `findById returns empty when subscription not found`() {
        `when`(subscriptionRepository.findById(99)).thenReturn(Optional.empty())

        val result = subscriptionService.findById(99)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `findAllActive returns active subscriptions for user`() {
        val pageable = Pageable.ofSize(10)

        `when`(subscriptionRepository.findAllByUserIdAndStatus(42, SubscriptionStatus.ACTIVE, pageable))
            .thenReturn(PageImpl(listOf(subscription)))
        `when`(subscriptionMapper.map(subscription)).thenReturn(readDto)

        val result = subscriptionService.findAllActive(42, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(readDto, result.content.first())
    }

    @Test
    fun `findAllActive returns empty page for user with no active subscriptions`() {
        val pageable = Pageable.ofSize(10)

        `when`(subscriptionRepository.findAllByUserIdAndStatus(99, SubscriptionStatus.ACTIVE, pageable))
            .thenReturn(PageImpl(emptyList()))

        val result = subscriptionService.findAllActive(99, pageable)

        assertTrue(result.isEmpty)
    }

    @Test
    fun `create returns dto when subscription is new`() {
        val createDto = SubscriptionCreateDto(userId = 42, productId = 10, Instant.now())

        `when`(subscriptionRepository.existsByUserIdAndProductId(42, 10)).thenReturn(false)
        `when`(productRepository.findById(10)).thenReturn(Optional.of(product))
        `when`(subscriptionMapper.map(createDto, product)).thenReturn(subscription)
        `when`(subscriptionRepository.save(subscription)).thenReturn(subscription)
        `when`(subscriptionMapper.map(subscription)).thenReturn(readDto)

        val result = subscriptionService.create(createDto)

        assertTrue(result.isPresent)
        assertEquals(readDto, result.get())
        verify(subscriptionRepository).save(subscription)
    }

    @Test
    fun `create throws when subscription already exists`() {
        val createDto = SubscriptionCreateDto(userId = 42, productId = 10, Instant.now())

        `when`(subscriptionRepository.existsByUserIdAndProductId(42, 10)).thenReturn(true)

        assertThrows<IllegalArgumentException> {
            subscriptionService.create(createDto)
        }

        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `create returns empty when product not found`() {
        val createDto = SubscriptionCreateDto(userId = 42, productId = 999, Instant.now())

        `when`(subscriptionRepository.existsByUserIdAndProductId(42, 999)).thenReturn(false)
        `when`(productRepository.findById(999)).thenReturn(Optional.empty())

        val result = subscriptionService.create(createDto)

        assertTrue(result.isEmpty)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `cancel sets status to CANCELLED and clears dates`() {
        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.cancel(1)

        assertTrue(result)
        assertEquals(SubscriptionStatus.CANCELLED, subscription.status)
        assertNull(subscription.expiresAt)
        assertNull(subscription.pausedAt)
        verify(subscriptionRepository).save(subscription)
    }

    @Test
    fun `cancel returns false when subscription not found`() {
        `when`(subscriptionRepository.findById(99)).thenReturn(Optional.empty())

        val result = subscriptionService.cancel(99)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `pause sets status to PAUSED and records pausedAt`() {
        val before = Instant.now()
        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.pause(1)

        assertTrue(result)
        assertEquals(SubscriptionStatus.PAUSED, subscription.status)
        assertNotNull(subscription.pausedAt)
        assertTrue(subscription.pausedAt!!.isAfter(before) || subscription.pausedAt!! == before)
        verify(subscriptionRepository).save(subscription)
    }

    @Test
    fun `pause returns false when subscription not found`() {
        `when`(subscriptionRepository.findById(99)).thenReturn(Optional.empty())

        val result = subscriptionService.pause(99)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `resume PAUSED subscription extends expiresAt by pause duration`() {
        val pausedAt = Instant.now().minus(2, ChronoUnit.HOURS)
        val originalExpiry = Instant.now().plus(10, ChronoUnit.DAYS)

        subscription.status = SubscriptionStatus.PAUSED
        subscription.pausedAt = pausedAt
        subscription.expiresAt = originalExpiry

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, null)

        assertTrue(result)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
        assertNull(subscription.pausedAt)
        assertTrue(subscription.expiresAt!!.isAfter(originalExpiry))
        verify(subscriptionRepository).save(subscription)
    }

    @Test
    fun `resume PAUSED subscription with null pausedAt returns false`() {
        subscription.status = SubscriptionStatus.PAUSED
        subscription.pausedAt = null

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, null)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `resume CANCELLED subscription with future expiresAt reactivates`() {
        val futureExpiry = Instant.now().plus(30, ChronoUnit.DAYS)
        subscription.status = SubscriptionStatus.CANCELLED

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, futureExpiry)

        assertTrue(result)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
        assertEquals(futureExpiry, subscription.expiresAt)
        verify(subscriptionRepository).save(subscription)
    }

    @Test
    fun `resume EXPIRED subscription with future expiresAt reactivates`() {
        val futureExpiry = Instant.now().plus(7, ChronoUnit.DAYS)
        subscription.status = SubscriptionStatus.EXPIRED

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, futureExpiry)

        assertTrue(result)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
        assertEquals(futureExpiry, subscription.expiresAt)
    }

    @Test
    fun `resume CANCELLED subscription with null expiresAt returns false`() {
        subscription.status = SubscriptionStatus.CANCELLED

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, null)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `resume CANCELLED subscription with past expiresAt returns false`() {
        val pastExpiry = Instant.now().minus(1, ChronoUnit.DAYS)
        subscription.status = SubscriptionStatus.CANCELLED

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, pastExpiry)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `resume ACTIVE subscription returns false`() {
        subscription.status = SubscriptionStatus.ACTIVE

        `when`(subscriptionRepository.findById(1)).thenReturn(Optional.of(subscription))

        val result = subscriptionService.resume(1, null)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }

    @Test
    fun `resume returns false when subscription not found`() {
        `when`(subscriptionRepository.findById(99)).thenReturn(Optional.empty())

        val result = subscriptionService.resume(99, null)

        assertFalse(result)
        verify(subscriptionRepository, never()).save(any())
    }
}