package br.com.microservices.orchestrated.inventoryservice.configs.exceptions;

public class OrderAlreadyExistsException extends RuntimeException {
    public OrderAlreadyExistsException(String message) {
        super(message);
    }
}
