package br.com.microservices.orchestrated.orderservice.core.services;

import br.com.microservices.orchestrated.orderservice.core.document.EventDocument;
import br.com.microservices.orchestrated.orderservice.core.document.OrderDocument;
import br.com.microservices.orchestrated.orderservice.core.document.OrderProductsDocument;
import br.com.microservices.orchestrated.orderservice.core.dtos.OrderRequestDto;
import br.com.microservices.orchestrated.orderservice.core.producers.SagaProducer;
import br.com.microservices.orchestrated.orderservice.core.repositories.OrderRepository;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private static final String TRANSACTION_ID_PATTERN = "%s-%s";

    private final OrderRepository orderRepository;
    private final JsonUtil jsonUtil;
    private final SagaProducer sagaProducer;
    private final EventService eventService;

    public OrderDocument save(OrderRequestDto orderRequestDto) {
        
        var orderDocument = OrderDocument
                .builder()
                .products(orderRequestDto.products()
                        .stream()
                        .map(OrderProductsDocument::getProduct)
                        .toList())
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .transactionId(
                        String.format(
                                TRANSACTION_ID_PATTERN,
                                Instant.now().toEpochMilli(),
                                UUID.randomUUID()
                        )
                )
                .build();

       orderRepository.save(orderDocument);

       sagaProducer.sendEvent(jsonUtil.toJson(createEventPayload(orderDocument)));

       return orderDocument;
    }

    private EventDocument createEventPayload(OrderDocument orderDocument) {
        var eventDocument = EventDocument
                .builder()
                .orderId(orderDocument.getOrderId())
                .transactionId(orderDocument.getTransactionId())
                .payload(orderDocument)
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .build();

        eventService.save(eventDocument);

        return eventDocument;
    }

}
