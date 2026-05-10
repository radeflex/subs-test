package ru.radeflex.substest.specs

import org.springframework.data.jpa.domain.Specification
import ru.radeflex.substest.entity.Product
import ru.radeflex.substest.entity.Subscription
import ru.radeflex.substest.entity.SubscriptionStatus
import ru.radeflex.substest.entity.User
import java.time.Instant

object SubscriptionSpecs {
    fun status(status: SubscriptionStatus?) =
        Specification<Subscription> { root, _, cb ->
            status?.let {
                cb.equal(root.get<SubscriptionStatus>("status"), it)
            }
        }
    fun userId(id: Int?) =
        Specification<Subscription> {root, _, cb  ->
            id?.let {
                cb.equal(root.get<User>("user").get<Int>("id"), it)
            }
        }
    fun productId(id: Int?) =
        Specification<Subscription> {root, _, cb  ->
            id?.let {
                cb.equal(root.get<Product>("product").get<Int>("id"), it)
            }
        }
    fun expiresAtBefore(instant: Instant?) =
        Specification<Subscription> {root, _, cb  ->
            instant?.let {
                cb.lessThanOrEqualTo(root.get("expiresAt"), it)
            }
        }
}