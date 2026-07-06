package br.com.microservices.orchestrated.paymentservice.core.dtos;

public record ProductDto(
        String code,
        double unitValue
) {
}
