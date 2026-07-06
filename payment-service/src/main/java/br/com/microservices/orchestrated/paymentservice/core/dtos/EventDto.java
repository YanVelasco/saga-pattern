package br.com.microservices.orchestrated.paymentservice.core.dtos;

import br.com.microservices.orchestrated.paymentservice.core.enums.SagaStatus;
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