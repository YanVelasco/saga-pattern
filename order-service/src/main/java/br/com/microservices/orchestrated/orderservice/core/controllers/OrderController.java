package br.com.microservices.orchestrated.orderservice.core.controllers;

import br.com.microservices.orchestrated.orderservice.core.document.OrderDocument;
import br.com.microservices.orchestrated.orderservice.core.dtos.OrderRequestDto;
import br.com.microservices.orchestrated.orderservice.core.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    @PostMapping
    public OrderDocument createOrder(
            @RequestBody OrderRequestDto orderRequestDto
    ) {
        return orderService.save(orderRequestDto);
    }

}
