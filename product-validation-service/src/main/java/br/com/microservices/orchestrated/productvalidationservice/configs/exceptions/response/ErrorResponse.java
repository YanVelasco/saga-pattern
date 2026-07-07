package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String timestamp,
        Map<String, String> details
) {

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), null);
    }

    public ErrorResponse(int value, String validationError, String message, Map<String, String> errors) {
        this(value, validationError, message, null, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                , errors);
    }


    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> details) {
        return new ErrorResponse(status, error, message, path,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), details);
    }
}
