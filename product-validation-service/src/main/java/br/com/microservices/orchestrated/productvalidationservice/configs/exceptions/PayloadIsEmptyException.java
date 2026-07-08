package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class PayloadIsEmptyException extends RuntimeException {
    public PayloadIsEmptyException(String message) {
        super(message);
    }
}
