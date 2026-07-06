package br.com.microservices.orchestrated.productvalidationservice.core.dtos;

public record OrderProductsDto(
        ProductDto product,
        int quantity
) {
}
