package br.com.microservices.orchestrated.inventoryservice.configs.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
