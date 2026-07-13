package br.com.microservices.orchestrated.orchestratorservice.core.services;

import br.com.microservices.orchestrated.orchestratorservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.orchestratorservice.core.dtos.HistoryDto;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics;
import br.com.microservices.orchestrated.orchestratorservice.core.producers.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EventSource.ORCHESTRATOR;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus.FAIL;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.SagaStatus.SUCCESS;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.Topics.NOTIFY_ENDING;

@Slf4j
@Service
@AllArgsConstructor
public class OrquestratorService {

    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer sagaOrchestratorProducer;
    private final SagaExecutionController sagaExecutionController;

    public void startSaga(EventDto eventDto) {
        log.info("Starting saga with event: {}", eventDto);
        eventDto = eventDto.toBuilder()
                .source(ORCHESTRATOR)
                .status(SUCCESS)
                .build();

        var topic = getTopic(eventDto);
        log.info("Topic: {}", topic);

        log.info("Sending event to topic: {} with event: {}", topic, eventDto);
        eventDto = addHistory(eventDto, "Starting saga with event: " + eventDto);

        sendToProducerWithTopic(eventDto, topic.getTopic());
    }

    public void finishSagaSuccess(EventDto eventDto) {
        log.info("Finishing saga with event: {}", eventDto);
        eventDto = eventDto.toBuilder()
                .source(ORCHESTRATOR)
                .status(SUCCESS)
                .build();

        log.info("Saga finished successfully with event: {}", eventDto);
        eventDto = addHistory(eventDto, "Finishing saga with event: " + eventDto);

        notifyEndingSaga(eventDto);
    }

    public void finishSagaFail(EventDto eventDto) {
        log.info("Finishing saga FAIL with errors for: {}", eventDto);
        eventDto = eventDto.toBuilder()
                .source(ORCHESTRATOR)
                .status(FAIL)
                .build();

        log.info("Saga finished with errors for event: {}", eventDto);
        eventDto = addHistory(eventDto, "Finishing saga FAIL with errors for event: " + eventDto);

        notifyEndingSaga(eventDto);
    }

    public void continueSaga(EventDto event) {
        var topic = getTopic(event);
        log.info("Continuing saga with event: {} to topic: {}", event, topic);

        sendToProducerWithTopic(event, topic.getTopic());
    }

    private Topics getTopic(EventDto eventDto) {
        return sagaExecutionController.getNextTopic(eventDto);
    }

    private EventDto addHistory(EventDto eventDto, String message) {
        var history = HistoryDto
                .builder()
                .message(message)
                .source(eventDto.source())
                .status(eventDto.status())
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .build();

        return eventDto.addToHistory(history);
    }

    private void notifyEndingSaga(EventDto eventDto) {
        sagaOrchestratorProducer.sendEvent(NOTIFY_ENDING.getTopic(), jsonUtil.toJson(eventDto));
    }

    private void sendToProducerWithTopic(EventDto eventDto, String topic) {
        sagaOrchestratorProducer.sendEvent(topic, jsonUtil.toJson(eventDto));
    }
}
