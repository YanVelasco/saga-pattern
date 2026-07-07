package br.com.microservices.orchestrated.paymentservice.core.utils;

import br.com.microservices.orchestrated.paymentservice.core.dtos.EventDto;
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

    public EventDto toEvent(String json) {
        try {
            log.info("Converting object to EVENT: {}", json);
            return objectMapper.readValue(json, EventDto.class);
        } catch (Exception e) {
            log.error("Error converting object to JSON: {}", json, e);
            return null;
        }
    }

}
