package br.com.microservices.orchestrated.paymentservice.configs.exceptions;

public class AmountValidationException extends RuntimeException {
    public AmountValidationException(String message) {
        super(message);
    }
}
