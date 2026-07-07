package br.com.microservices.orchestrated.orderservice.core.dtos;

public record FiltersDto(
        String orderId,
        String transactionId
) {
}
