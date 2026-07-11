package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.configs.exceptions.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.*;
import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
public class SagaExecutionController {

    private static final String SAGA_ID_FORMAT = "ORDER_ID: %s | TRANSACTION_ID: %s | EVENT_ID: %s";

    public Topics getNextTopic(EventDto eventDto) {
        if (isEmpty(eventDto.status()) || isEmpty(eventDto.source())) {
            throw new ValidationException("Status and source are required to determine the next topic.");
        }
        var topic = findTopicBySourceAndStatus(eventDto);
        logCurrentSagaStatus(eventDto, topic);
        return topic;
    }

    private Topics findTopicBySourceAndStatus(EventDto eventDto) {
        return Arrays.stream(SAGA_HANDLER)
                .filter(row -> isValidEvent(eventDto, row))
                .findFirst()
                .map(i -> (Topics) i[TOPIC_INDEX])
                .orElseThrow(() -> new ValidationException("No handler found for the given event."));
    }

    private boolean isValidEvent(EventDto eventDto, Object[] row) {
        var source = row[EVENT_SOURCE_INDEX];
        var status = row[SAGA_STATUS_INDEX];
        return eventDto.source().equals(source) && eventDto.status().equals(status);
    }

    private void logCurrentSagaStatus(EventDto eventDto, Topics topics) {
        var sagaId = createSagaId(eventDto);
        var source = eventDto.source();

        switch (eventDto.status()) {
            case SUCCESS ->
                    log.info("SAGA ID: {} | SOURCE: {} | STATUS: SUCCESS | NEXT TOPIC: {}", sagaId, source, topics);
            case ROLLBACK_PENDING ->
                    log.info("SAGA ID: {} | SOURCE: {} | STATUS: ROLLBACK_PENDING | NEXT TOPIC: {}", sagaId, source, topics);
            case FAIL ->
                    log.info("SAGA ID: {} | SOURCE: {} | STATUS: FAIL | NEXT TOPIC: {}", sagaId, source, topics);
            default ->
                    log.info("SAGA ID: {} | SOURCE: {} | STATUS: {} | NEXT TOPIC: {}", sagaId, source, eventDto.status(), topics);
        }
    }

    private String createSagaId(EventDto eventDto) {
        return format(SAGA_ID_FORMAT, eventDto.payload().orderId(), eventDto.transactionId(), eventDto.eventId());
    }

}
