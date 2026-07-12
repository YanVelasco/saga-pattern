package br.com.microservices.orchestrated.orchestratorservice.core.consumers;

import br.com.microservices.orchestrated.orchestratorservice.core.services.OrquestratorService;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SagaOrchestratorConsumer {

    private final JsonUtil jsonUtil;
    private final OrquestratorService orquestratorService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.start_saga}"
    )
    public void consumeStartSagaEvent(String payload) {
        log.info("Received start saga message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        orquestratorService.startSaga(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-success}"
    )
    public void consumeFinishSuccessEvent(String payload) {
        log.info("Received finish success message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        orquestratorService.finishSagaSuccess(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.finish-fail}"
    )
    public void consumeFinishFailEvent(String payload) {
        log.info("Received finish fail message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        orquestratorService.finishSagaFail(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.orchestrator}"
    )
    public void consumeOrchestratorEvent(String payload) {
        log.info("Received orchestrator message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        orquestratorService.continueSaga(event);
    }

}
