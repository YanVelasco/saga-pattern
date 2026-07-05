package br.com.microservices.orchestrated.orchestratorservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Topics {

    SART_SAGA("start-saga"),
    BASE_ORQUESTRATOR("orquestrator"),
    FINISH_SUCCESS("finish-success"),
    FINISH_FAIL("finish-fail"),
    PRODUCT_VALIDATION_SUCCESS("product-validation-success"),
    PRODUCT_VALIDATION_FAIL("product-validation-fail"),
    PAYMENT_VALIDATION_SUCCESS("payment-validation-success"),
    PAYMENT_VALIDATION_FAIL("payment-validation-fail"),
    INVENTORY_VALIDATION_SUCCESS("inventory-validation-success"),
    INVENTORY_VALIDATION_FAIL("inventory-validation-fail"),
    NOTIFY_ENDING("notify-ending");

    private final String topic;
}
