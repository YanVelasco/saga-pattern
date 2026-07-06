package br.com.microservices.orchestrated.productvalidationservice.core.dtos;


import br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus;
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