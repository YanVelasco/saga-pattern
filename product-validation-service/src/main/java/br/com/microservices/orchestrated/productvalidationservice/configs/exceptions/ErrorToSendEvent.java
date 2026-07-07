package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class ErrorToSendEvent extends RuntimeException {
    public ErrorToSendEvent(String message) {
        super(message);
    }
}
