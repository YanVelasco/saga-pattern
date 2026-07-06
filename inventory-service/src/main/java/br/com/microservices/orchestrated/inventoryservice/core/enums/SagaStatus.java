package br.com.microservices.orchestrated.inventoryservice.core.enums;

public enum SagaStatus {
    SUCCESS,
    ROLLBACK_PENDING,
    FAIL
}
