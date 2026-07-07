package br.com.microservices.orchestrated.orderservice.core.producers;

import br.com.microservices.orchestrated.orderservice.configs.exceptions.ErrorToSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.start-saga}")
    private String startSagaTopic;

    public void sendEvent(String payload) {
        try {
            kafkaTemplate.send(startSagaTopic, payload);
            log.info("Event sent to topic {}: , with Data {}:", startSagaTopic, payload);
        } catch (Exception e) {
            log.error("Failed to send event to Topic {}: , with Data: {}", startSagaTopic, payload, e);
            throw new ErrorToSendEvent("Failed to send event to Topic " + startSagaTopic + ", with Data: " + payload);
        }
    }
}
