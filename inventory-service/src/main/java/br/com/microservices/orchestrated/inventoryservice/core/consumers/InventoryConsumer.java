package br.com.microservices.orchestrated.inventoryservice.core.consumers;

import br.com.microservices.orchestrated.inventoryservice.core.services.InventoryService;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class InventoryConsumer {

    private final JsonUtil jsonUtil;
    private final InventoryService inventoryService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-success}"
    )
    public void consumeInventorySuccessEvent(String payload) {
        log.info("Received inventory success message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        inventoryService.updateInventory(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.inventory-fail}"
    )
    public void consumeInventoryFailEvent(String payload) {
        log.info("Received inventory fail message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        inventoryService.rollbackinventory(event);
    }

}
