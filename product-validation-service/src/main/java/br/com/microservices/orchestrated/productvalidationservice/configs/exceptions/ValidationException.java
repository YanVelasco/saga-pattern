package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldErrors = Collections.emptyMap();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}

