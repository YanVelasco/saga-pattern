package br.com.microservices.orchestrated.productvalidationservice.core.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDto(
        String orderId,
        List<OrderProductsDto> products,
        LocalDateTime createdAt,
        String transactionId,
        double totalAmount,
        int totalItems
) {
}
