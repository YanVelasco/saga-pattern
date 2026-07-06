package br.com.microservices.orchestrated.paymentservice.core.enums;

public enum SagaStatus {
    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
