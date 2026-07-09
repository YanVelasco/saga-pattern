package br.com.microservices.orchestrated.paymentservice.core.dtos;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
public record OrderDto(
        String orderId,
        List<OrderProductsDto> products,
        LocalDateTime createdAt,
        String transactionId,
        double totalAmount,
        int totalItems
) {
}
