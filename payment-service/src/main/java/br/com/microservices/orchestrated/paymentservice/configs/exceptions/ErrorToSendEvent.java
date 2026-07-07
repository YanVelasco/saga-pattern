package br.com.microservices.orchestrated.paymentservice.configs.exceptions;

public class ErrorToSendEvent extends RuntimeException {
    public ErrorToSendEvent(String message) {
        super(message);
    }
}
