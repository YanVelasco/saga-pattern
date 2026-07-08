package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
