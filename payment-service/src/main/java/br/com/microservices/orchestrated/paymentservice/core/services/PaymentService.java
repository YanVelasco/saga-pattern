package br.com.microservices.orchestrated.paymentservice.core.services;

import br.com.microservices.orchestrated.paymentservice.configs.exceptions.PaymentAlreadyExists;
import br.com.microservices.orchestrated.paymentservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.paymentservice.core.dtos.OrderProductsDto;
import br.com.microservices.orchestrated.paymentservice.core.model.PaymentModel;
import br.com.microservices.orchestrated.paymentservice.core.producers.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repositories.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final PaymentRepository paymentRepository;

    public void realizePayment(EventDto eventDto) {

        try {
            log.info("Realizing payment for event: {}", eventDto);
            checkCurrentValidation(eventDto);
            eventDto = createPendingPayment(eventDto);
        } catch (Exception e) {
            log.error("Error realizing payment for event: {}", eventDto, e);
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(eventDto));
    }

    private void checkCurrentValidation(EventDto eventDto) {

        if (paymentRepository.existsByOrderIdAndTransactionId(eventDto.orderId(), eventDto.transactionId())) {
            log.info("Payment already processed for event: {}", eventDto);
            throw new PaymentAlreadyExists("Payment already processed for orderId: " + eventDto.orderId() + " and transactionId: " + eventDto.transactionId());
        }

    }

    private EventDto createPendingPayment(EventDto eventDto) {
        var totalAmount = calculateAmount(eventDto);
        var totalItems = calculateTotalItems(eventDto);

        var payment = PaymentModel.builder()
                .orderId(eventDto.orderId())
                .transactionId(eventDto.transactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        savePayment(payment);

        return setEventAmountItems(eventDto, payment);
    }

    private void savePayment(PaymentModel paymentModel) {
        paymentRepository.save(paymentModel);
    }

    private double calculateAmount(EventDto eventDto) {
        return eventDto
                .payload()
                .products()
                .stream()
                .mapToDouble(
                        product -> product.quantity() * product.product().unitValue()
                )
                .sum();
    }

    private int calculateTotalItems(EventDto eventDto) {
        return eventDto
                .payload()
                .products()
                .stream()
                .mapToInt(
                        OrderProductsDto::quantity
                )
                .sum();
    }

    private EventDto setEventAmountItems(EventDto eventDto, PaymentModel paymentModel) {
        return eventDto.toBuilder()
                .payload(eventDto.payload().toBuilder()
                        .totalAmount(paymentModel.getTotalAmount())
                        .totalItems(paymentModel.getTotalItems())
                        .build())
                .build();
    }

}
