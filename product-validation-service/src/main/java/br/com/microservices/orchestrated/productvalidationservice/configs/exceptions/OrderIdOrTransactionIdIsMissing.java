package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class OrderIdOrTransactionIdIsMissing extends RuntimeException {
    public OrderIdOrTransactionIdIsMissing(String message) {
        super(message);
    }
}
