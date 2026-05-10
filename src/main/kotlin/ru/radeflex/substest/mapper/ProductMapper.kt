package ru.radeflex.substest.mapper

import org.springframework.stereotype.Component
import ru.radeflex.substest.dto.ProductCreateDto
import ru.radeflex.substest.dto.ProductReadDto
import ru.radeflex.substest.entity.Product

@Component
class ProductMapper {
    fun map(dto: ProductCreateDto): Product {
        return Product(
            name = dto.name,
            price = dto.price,
        )
    }
    fun map(product: Product): ProductReadDto {
        return ProductReadDto(
            id = product.id,
            name = product.name,
            price = product.price,
        )
    }
}
