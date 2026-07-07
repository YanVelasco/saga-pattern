package br.com.microservices.orchestrated.orderservice.core.services;

import br.com.microservices.orchestrated.orderservice.core.document.EventDocument;
import br.com.microservices.orchestrated.orderservice.core.repositories.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void notifyEnding(EventDocument eventDocument) {
        eventDocument.setOrderId(eventDocument.getOrderId());
        eventDocument.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        save(eventDocument);
        log.info("Event order {} with SAGA NOTIFIED: {}", eventDocument.getOrderId(), eventDocument.getTransactionId());
    }

    public EventDocument save(EventDocument eventDocument) {
        return eventRepository.save(eventDocument);
    }

}
