package br.com.microservices.orchestrated.orderservice.core.services;

import br.com.microservices.orchestrated.orderservice.configs.exceptions.EventNotFoundException;
import br.com.microservices.orchestrated.orderservice.core.document.EventDocument;
import br.com.microservices.orchestrated.orderservice.core.dtos.FiltersDto;
import br.com.microservices.orchestrated.orderservice.core.repositories.EventRepository;
import br.com.microservices.orchestrated.orderservice.core.specifications.FilterSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final MongoTemplate mongoTemplate;

    public void notifyEnding(EventDocument eventDocument) {
        eventDocument.setOrderId(eventDocument.getOrderId());
        eventDocument.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        save(eventDocument);
        log.info("Event order {} with SAGA NOTIFIED: {}", eventDocument.getOrderId(), eventDocument.getTransactionId());
    }

    public EventDocument save(EventDocument eventDocument) {
        return eventRepository.save(eventDocument);
    }

    public List<EventDocument> findAll(FiltersDto filtersDto) {
        Query query = FilterSpecification.eventFilters(filtersDto);
        List<EventDocument> events = mongoTemplate.find(query, EventDocument.class);

        if (hasFilters(filtersDto) && events.isEmpty()) {
            throw new EventNotFoundException("Nenhum evento encontrado com os filtros fornecidos");
        }

        return events;
    }

    private boolean hasFilters(FiltersDto filtersDto) {
        return filtersDto != null &&
                (filtersDto.orderId() != null || filtersDto.transactionId() != null);
    }
}
