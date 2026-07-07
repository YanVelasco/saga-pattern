package br.com.microservices.orchestrated.orderservice.core.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "events")
public class EventDocument {
    @Id
    private String eventId;
    private String transactionId;
    private String orderId;
    private OrderDocument payload;
    private String source;
    private String status;
    private List<HistoryDocument> eventHistory;
    private LocalDateTime createdAt;

}
