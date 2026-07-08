package br.com.microservices.orchestrated.productvalidationservice.core.dtos;

import br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
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
    public EventDto addToHistory(HistoryDto history) {
        List<HistoryDto> newHistory = new ArrayList<>();
        
        if (this.eventHistory != null) {
            newHistory.addAll(this.eventHistory);
        }
        
        newHistory.add(history);

        return this.toBuilder()
                .eventHistory(newHistory)
                .build();
    }
}