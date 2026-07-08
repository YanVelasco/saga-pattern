package br.com.microservices.orchestrated.productvalidationservice.configs.exceptions;

public class ProductOrCodeIsMissing extends RuntimeException {
    public ProductOrCodeIsMissing(String message) {
        super(message);
    }
}
