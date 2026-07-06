package br.com.microservices.orchestrated.paymentservice.core.dtos;

import br.com.microservices.orchestrated.paymentservice.core.enums.SagaStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HistoryDto(
        String source,
        SagaStatus status,
        String message,
        LocalDateTime createdAt
) {
}