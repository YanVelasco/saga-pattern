package br.com.microservices.orchestrated.inventoryservice.core.dtos;

public record ProductDto(
        String code,
        double unitValue
) {
}
