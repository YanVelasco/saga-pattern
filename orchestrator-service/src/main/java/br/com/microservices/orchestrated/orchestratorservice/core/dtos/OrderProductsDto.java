package br.com.microservices.orchestrated.orchestratorservice.core.dtos;

public record OrderProductsDto(
        ProductDto product,
        int quantity
) {
}
