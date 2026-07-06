package br.com.microservices.orchestrated.productvalidationservice.core.dtos;

import br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EventDto(
        String eventId,
        String transactionId,
        String orderId,
        OrderDto payload,
        String source,
        SagaStatus status,
        List<HistoryDto> eventHistory,
        LocalDateTime createdAt
) {
}