package br.com.microservices.orchestrated.orchestratorservice.core.producers;

import br.com.microservices.orchestrated.orchestratorservice.configs.exceptions.ErrorToSendEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SagaOrchestratorProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(String topic, String payload) {
        try {
            kafkaTemplate.send(topic, payload);
            log.info("Event sent to topic {}: , with Data {}:", topic, payload);
        } catch (Exception e) {
            log.error("Failed to send event to Topic {}: , with Data: {}", topic, payload, e);
            throw new ErrorToSendEvent("Failed to send event to Topic " + topic + ", with Data: " + payload);
        }
    }
}
