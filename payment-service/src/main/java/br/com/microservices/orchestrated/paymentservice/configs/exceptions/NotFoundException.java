package br.com.microservices.orchestrated.paymentservice.configs.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
