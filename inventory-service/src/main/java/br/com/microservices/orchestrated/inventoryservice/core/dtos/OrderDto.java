package br.com.microservices.orchestrated.inventoryservice.core.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderDto(
        String orderId,
        List<ProductDto> products,
        LocalDateTime createdAt,
        String transactionId,
        double totalAmount,
        int totalItems
) {
}
