package ru.radeflex.substest.http.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.radeflex.substest.dto.SubscriptionCreateDto
import ru.radeflex.substest.dto.SubscriptionFilter
import ru.radeflex.substest.dto.SubscriptionReadDto
import ru.radeflex.substest.service.SubscriptionService
import java.time.Instant

@RestController
@RequestMapping("/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @GetMapping
    fun findAll(filter: SubscriptionFilter, pageable: Pageable): ResponseEntity<Page<SubscriptionReadDto>> {
        return ResponseEntity.ok(subscriptionService.findAll(filter, pageable))
    }

    @GetMapping("/active/{userId}")
    fun findAllActive(@PathVariable userId: Int, pageable: Pageable): ResponseEntity<Page<SubscriptionReadDto>> {
        return ResponseEntity.ok(subscriptionService.findAllActive(userId, pageable))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<SubscriptionReadDto> {
        return subscriptionService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElseGet{ ResponseEntity(HttpStatus.NOT_FOUND) }
    }

    @PostMapping
    fun create(@RequestBody @Valid dto: SubscriptionCreateDto): ResponseEntity<SubscriptionReadDto> {
        return subscriptionService.create(dto)
            .map { ResponseEntity.ok(it) }
            .orElseGet{ ResponseEntity(HttpStatus.NOT_FOUND) }
    }

    @DeleteMapping("{id}/cancel")
    fun cancel(@PathVariable id: Int): ResponseEntity<Void> {
        if (!subscriptionService.cancel(id))
            return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok().build()
    }

    @PutMapping("{id}/pause")
    fun pause(@PathVariable id: Int): ResponseEntity<Void> {
        if (!subscriptionService.pause(id))
            return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok().build()
    }

    @PutMapping("{id}/resume")
    fun resume(@PathVariable id: Int, expiresAt: Instant?): ResponseEntity<Void> {
        if (!subscriptionService.resume(id, expiresAt))
            return ResponseEntity(HttpStatus.BAD_REQUEST)
        return ResponseEntity.ok().build()
    }
}