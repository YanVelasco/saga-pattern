package br.com.microservices.orchestrated.orchestratorservice.core.dtos;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record EventDto(
        String eventId,
        String transactionId,
        String orderId,
        OrderDto payload,
        EventSource source,
        SagaStatus status,
        List<HistoryDto> eventHistory,
        LocalDateTime createdAt
) {
}