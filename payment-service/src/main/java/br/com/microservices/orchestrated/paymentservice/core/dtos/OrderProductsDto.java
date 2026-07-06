package br.com.microservices.orchestrated.paymentservice.core.dtos;

public record OrderProductsDto(
        ProductDto product,
        int quantity
) {
}
