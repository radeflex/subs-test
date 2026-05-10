package ru.radeflex.substest.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import ru.radeflex.substest.dto.ProductCreateDto
import ru.radeflex.substest.dto.ProductReadDto
import ru.radeflex.substest.mapper.ProductMapper
import ru.radeflex.substest.repository.ProductRepository

@Service
class ProductService(
    private val productMapper: ProductMapper,
    private val productRepository: ProductRepository
) {

    fun findAll(pageable: Pageable): Page<ProductReadDto> =
        productRepository.findAll(pageable)
            .map { productMapper.map(it) }

    fun create(dto: ProductCreateDto): ProductReadDto {
        if (productRepository.existsByName(dto.name))
            throw IllegalArgumentException("product with such name already exists")
        val mapped = productMapper.map(dto)
        return productMapper.map(productRepository.save(mapped))
    }


    fun delete(id: Int): Boolean {
        if (!productRepository.existsById(id)) {
            return false
        }
        productRepository.deleteById(id)
        return true
    }
}