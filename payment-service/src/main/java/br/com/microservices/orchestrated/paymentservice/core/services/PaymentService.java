package br.com.microservices.orchestrated.paymentservice.core.services;

import br.com.microservices.orchestrated.paymentservice.configs.exceptions.AmountValidationException;
import br.com.microservices.orchestrated.paymentservice.configs.exceptions.NotFoundException;
import br.com.microservices.orchestrated.paymentservice.configs.exceptions.PaymentAlreadyExists;
import br.com.microservices.orchestrated.paymentservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.paymentservice.core.dtos.HistoryDto;
import br.com.microservices.orchestrated.paymentservice.core.dtos.OrderProductsDto;
import br.com.microservices.orchestrated.paymentservice.core.dtos.PaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.PaymentModel;
import br.com.microservices.orchestrated.paymentservice.core.producers.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repositories.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.microservices.orchestrated.paymentservice.core.enums.SagaStatus.FAIL;
import static br.com.microservices.orchestrated.paymentservice.core.enums.SagaStatus.ROLLBACK_PENDING;
import static br.com.microservices.orchestrated.paymentservice.core.enums.SagaStatus.SUCCESS;

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
            var payment = createPendingPayment(eventDto);
            eventDto = setEventAmountItems(eventDto, payment);
            validateAmount(payment.getTotalAmount());
            eventDto = handleSuccess(eventDto);
            changePaymentToSuccessStatus(payment);
        } catch (Exception e) {
            log.error("Error realizing payment for event: {}", eventDto, e);
            eventDto = handleFailCurrentNotExecuted(eventDto, e.getMessage());
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(eventDto));
    }

    private void checkCurrentValidation(EventDto eventDto) {

        if (paymentRepository.existsByOrderIdAndTransactionId(eventDto.orderId(), eventDto.transactionId())) {
            log.info("Payment already processed for event: {}", eventDto);
            throw new PaymentAlreadyExists("Payment already processed for orderId: " + eventDto.orderId() + " and transactionId: " + eventDto.transactionId());
        }

    }

    private PaymentModel createPendingPayment(EventDto eventDto) {
        var totalAmount = calculateAmount(eventDto);
        var totalItems = calculateTotalItems(eventDto);

        var payment = PaymentModel.builder()
                .orderId(eventDto.orderId())
                .transactionId(eventDto.transactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        savePayment(payment);

        return payment;
    }

    private void savePayment(PaymentModel paymentModel) {
        paymentRepository.save(paymentModel);
    }

    private double calculateAmount(EventDto eventDto) {
        var totalAmount = eventDto
                .payload()
                .products()
                .stream()
                .map(product -> BigDecimal.valueOf(product.product().unitValue())
                        .multiply(BigDecimal.valueOf(product.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmount.setScale(2, RoundingMode.HALF_UP).doubleValue();
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

    private PaymentModel findPaymentByOrderIdAndTransactionId(EventDto eventDto) {
        return paymentRepository.findByOrderIdAndTransactionId(eventDto.orderId(), eventDto.transactionId())
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + eventDto.orderId() + " and transactionId: " + eventDto.transactionId()));
    }

    private void validateAmount(double amount) {
        if (amount <= 0.1) {
            throw new AmountValidationException("Amount must be greater than zero. Provided amount: " + amount);
        }
    }

    private void changePaymentToSuccessStatus(PaymentModel paymentModel) {
        paymentModel.setPaymentStatus(PaymentStatus.SUCCESS);
        savePayment(paymentModel);
    }

    private EventDto handleSuccess(EventDto eventDto) {
        eventDto = eventDto.toBuilder()
                .status(SUCCESS)
                .source(CURRENT_SOURCE)
                .build();

        eventDto = addHistory(eventDto, "Payment realized successfully");

        return eventDto;
    }

    private EventDto addHistory(EventDto eventDto, String message) {
        var history = HistoryDto
                .builder()
                .message(message)
                .source(eventDto.source())
                .status(eventDto.status())
                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                .build();

        return eventDto.addToHistory(history);
    }

    private EventDto handleFailCurrentNotExecuted(EventDto eventDto, String message) {
        eventDto = eventDto.toBuilder()
                .status(ROLLBACK_PENDING)
                .source(CURRENT_SOURCE)
                .build();

        eventDto = addHistory(eventDto, "Fail to realize payment: " + message);

        return eventDto;
    }

    public void rollbackEvent(EventDto eventDto) {
        try {
            eventDto = changePaymentStatusToRefunded(eventDto);
            log.info("Rollback completed for orderId: {}", eventDto.orderId());
        } catch (Exception e) {
            log.error("Error rolling back payment, but continuing rollback: {}", e.getMessage());
        }

        var eventToRollback = eventDto.toBuilder()
                .status(FAIL)
                .source(CURRENT_SOURCE)
                .build();

        eventToRollback = addHistory(eventToRollback, "Rollback executed for event: " + eventDto.eventId() + " on payment");

        kafkaProducer.sendEvent(jsonUtil.toJson(eventToRollback));
    }

    private EventDto changePaymentStatusToRefunded(EventDto eventDto) {
        var payment = findPaymentByOrderIdAndTransactionId(eventDto);
        payment.setPaymentStatus(PaymentStatus.REFUND);
        savePayment(payment);
        return setEventAmountItems(eventDto, payment);
    }

}
