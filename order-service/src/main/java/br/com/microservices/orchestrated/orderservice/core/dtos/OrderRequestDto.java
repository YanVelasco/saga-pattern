package br.com.microservices.orchestrated.orderservice.core.dtos;

import br.com.microservices.orchestrated.orderservice.core.document.OrderProductsDocument;

import java.util.List;

public record OrderRequestDto(
        List<OrderProductsDocument> products
) {
}
