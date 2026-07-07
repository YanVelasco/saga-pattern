package br.com.microservices.orchestrated.orderservice.core.repositories;

import br.com.microservices.orchestrated.orderservice.core.document.EventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<EventDocument, String> {
}
