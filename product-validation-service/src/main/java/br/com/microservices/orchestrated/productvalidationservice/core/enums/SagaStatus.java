package br.com.microservices.orchestrated.productvalidationservice.core.enums;

public enum SagaStatus {
    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
