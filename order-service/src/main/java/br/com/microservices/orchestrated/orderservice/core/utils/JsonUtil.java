package br.com.microservices.orchestrated.orderservice.core.utils;

import br.com.microservices.orchestrated.orderservice.core.document.EventDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        try {
            log.info("Converting object to JSON: {}", object);
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error converting object to JSON: {}", object, e);
            return e.getMessage();
        }
    }

    public EventDocument toEvent(String json) {
        try {
            log.info("Converting object to Event: {}", json);
            return objectMapper.readValue(json, EventDocument.class);
        } catch (Exception e) {
            log.error("Error converting object to JSON: {}", json, e);
            return null;
        }
    }

}
