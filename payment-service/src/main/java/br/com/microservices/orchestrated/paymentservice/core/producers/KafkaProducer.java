package br.com.microservices.orchestrated.paymentservice.core.producers;

import br.com.microservices.orchestrated.paymentservice.configs.exceptions.ErrorToSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;

    public void sendEvent(String payload) {
        try {
            kafkaTemplate.send(orchestratorTopic, payload);
            log.info("Event sent to topic {}: , with Data {}:", orchestratorTopic, payload);
        } catch (Exception e) {
            log.error("Failed to send event to Topic {}: , with Data: {}", orchestratorTopic, payload, e);
            throw new ErrorToSendEvent("Failed to send event to Topic " + orchestratorTopic + ", with Data: " + payload);
        }
    }
}
