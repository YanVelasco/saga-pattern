package br.com.microservices.orchestrated.paymentservice.configs.exceptions;

public class PaymentAlreadyExists extends RuntimeException {
    public PaymentAlreadyExists(String message) {
        super(message);
    }
}
