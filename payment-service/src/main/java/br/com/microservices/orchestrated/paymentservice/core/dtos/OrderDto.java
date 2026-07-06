package br.com.microservices.orchestrated.paymentservice.core.dtos;

import br.com.microservices.orchestrated.productvalidationservice.core.dtos.ProductDto;
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
