package br.com.microservices.orchestrated.orchestratorservice.configs.exceptions;

public class ErrorToSendEvent extends RuntimeException {
    public ErrorToSendEvent(String message) {
        super(message);
    }
}
