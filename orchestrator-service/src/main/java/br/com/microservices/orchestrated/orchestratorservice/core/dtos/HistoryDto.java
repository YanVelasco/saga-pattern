package br.com.microservices.orchestrated.orchestratorservice.core.dtos;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HistoryDto(
        EventSource source,
        SagaStatus status,
        String message,
        LocalDateTime createdAt
) {
}