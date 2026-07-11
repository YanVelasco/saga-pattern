package br.com.microservices.orchestrated.inventoryservice.configs.exceptions;

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
