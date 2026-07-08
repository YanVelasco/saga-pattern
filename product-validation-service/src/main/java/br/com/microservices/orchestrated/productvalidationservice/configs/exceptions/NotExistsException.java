package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class NotExistsException extends RuntimeException {
    public NotExistsException(String message) {
        super(message);
    }
}
