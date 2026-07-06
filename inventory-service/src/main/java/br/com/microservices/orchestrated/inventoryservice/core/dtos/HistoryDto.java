package br.com.microservices.orchestrated.inventoryservice.core.dtos;

import br.com.microservices.orchestrated.inventoryservice.core.enums.SagaStatus;
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