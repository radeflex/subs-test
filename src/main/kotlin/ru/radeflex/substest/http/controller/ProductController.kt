package ru.radeflex.substest.http.controller

import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.radeflex.substest.dto.ProductCreateDto
import ru.radeflex.substest.dto.ProductReadDto
import ru.radeflex.substest.service.ProductService

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {
    @GetMapping
    fun findAll(pageable: Pageable): ResponseEntity<Page<ProductReadDto>> =
        ResponseEntity.ok(productService.findAll(pageable))


    @PostMapping
    fun create(@RequestBody @Valid dto: ProductCreateDto):
            ResponseEntity<ProductReadDto> =
        ResponseEntity.ok(productService.create(dto))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Void> {
        return if (productService.delete(id)) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}