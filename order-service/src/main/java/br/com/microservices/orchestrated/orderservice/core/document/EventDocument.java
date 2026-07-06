package br.com.microservices.orchestrated.orderservice.core.document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDocument {

    private String eventId;
    private String transactionId;
    private String orderId;
    private OrderDocument payload;
    private String source;
    private String status;
    private List<HistoryDocument> eventHistory;
    private LocalDateTime createdAt;

}
