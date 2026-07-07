package br.com.microservices.orchestrated.orderservice.configs.exceptions;

public class ErrorToSendEvent extends RuntimeException {
    public ErrorToSendEvent(String message) {
        super(message);
    }
}
