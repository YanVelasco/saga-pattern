package br.com.microservices.orchestrated.inventoryservice.configs.exceptions;

public class ErrorToSendEvent extends RuntimeException {
    public ErrorToSendEvent(String message) {
        super(message);
    }
}