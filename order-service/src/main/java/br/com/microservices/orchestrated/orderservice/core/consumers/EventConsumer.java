package br.com.microservices.orchestrated.orderservice.core.consumers;

import br.com.microservices.orchestrated.orderservice.core.services.EventService;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;
    private final EventService eventService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumeNotifyEvent(String payload) {
        log.info("Received ending notification message: {}", payload);
        var event = jsonUtil.toEvent(payload);
        eventService.notifyEnding(event);
    }

}
