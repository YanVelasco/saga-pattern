package br.com.microservices.orchestrated.productvalidationservice.core.consumers;

import br.com.microservices.orchestrated.productvalidationservice.core.services.ProductValidationService;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final ProductValidationService productValidationService;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-success}"
    )
    public void consumeProductValidationSuccessEvent(String payload) {
        log.info("Received product validation success message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        productValidationService.validateExistingProducts(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}"
    )
    public void consumeProductValidationFailEvent(String payload) {
        log.info("Received product validation fail message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        productValidationService.rollbackEvent(event);
    }

}
