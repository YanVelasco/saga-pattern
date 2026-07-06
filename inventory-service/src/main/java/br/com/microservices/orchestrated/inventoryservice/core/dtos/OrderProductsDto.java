package br.com.microservices.orchestrated.inventoryservice.core.dtos;

public record OrderProductsDto(
        ProductDto product,
        int quantity
) {
}
