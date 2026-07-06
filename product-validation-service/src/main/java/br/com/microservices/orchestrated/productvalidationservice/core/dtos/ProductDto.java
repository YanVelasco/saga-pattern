package br.com.microservices.orchestrated.productvalidationservice.core.dtos;

public record ProductDto(
        String code,
        double unitValue
) {
}
