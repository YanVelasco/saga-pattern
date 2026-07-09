package br.com.microservices.orchestrated.productvalidationservice.core.services;

import br.com.microservices.orchestrated.productvalidationservice.configs.exceptions.*;
import br.com.microservices.orchestrated.productvalidationservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.productvalidationservice.core.dtos.HistoryDto;
import br.com.microservices.orchestrated.productvalidationservice.core.dtos.ProductDto;
import br.com.microservices.orchestrated.productvalidationservice.core.model.ValidationModel;
import br.com.microservices.orchestrated.productvalidationservice.core.producers.KafkaProducer;
import br.com.microservices.orchestrated.productvalidationservice.core.repositories.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repositories.ValidationRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final ProductRepository productRepository;
    private final ValidationRepository validationRepository;

    public void validateExistingProducts(EventDto eventDto) {

        try {
            log.info("Validating existing products for event: {}", eventDto);
            checkCurrentValidation(eventDto);
            createValidation(eventDto, true);
            eventDto = handleSuccess(eventDto);
        } catch (Exception e) {
            log.error("Error validating existing products for event: {}", eventDto, e);
            eventDto = handleFailCurrentNotExecuted(eventDto, e.getMessage());
        }

        kafkaProducer.sendEvent(jsonUtil.toJson(eventDto));
    }

    private void checkCurrentValidation(EventDto eventDto) {
        handleCurrentValidation(eventDto);

        if (validationRepository.existsByOrderIdAndTransactionId(eventDto.payload().orderId(), eventDto.payload().transactionId())) {
            throw new AlreadyExistsException("Validation already exists for orderId: " + eventDto.payload().orderId() +
                    " and transactionId: " + eventDto.payload().transactionId());
        }

        eventDto.payload().products().forEach(product -> {
            validateProductInformed(product);
            validateExistingProducts(product.code());
        });
    }

    private void handleCurrentValidation(EventDto eventDto) {
        if (isEmpty(eventDto.payload()) || isEmpty(eventDto.payload().products())) {
            throw new PayloadIsEmptyException("Payload or products list is empty");
        }

        if (isEmpty(eventDto.payload().orderId()) || isEmpty(eventDto.payload().transactionId())) {
            throw new OrderIdOrTransactionIdIsMissing("Order ID or Transaction ID is empty");
        }
    }

    private void validateProductInformed(ProductDto productDto) {
        if (isEmpty(productDto) || isEmpty(productDto.code())) {
            throw new ProductOrCodeIsMissing("Product or product code is empty");
        }
    }

    private void validateExistingProducts(String code) {
        if (!productRepository.existsByCode(code)) {
            throw new NotExistsException("Product with code " + code + " does not exist");
        }
    }

    private void createValidation(EventDto eventDto, boolean success) {
        var validation = ValidationModel.builder()
                .orderId(eventDto.payload().orderId())
                .transactionId(eventDto.payload().transactionId())
                .success(success)
                .build();

        validationRepository.save(validation);
    }

    private EventDto handleSuccess(EventDto eventDto) {
        eventDto = eventDto.toBuilder()
                .status(SUCCESS)
                .source(CURRENT_SOURCE)
                .build();

        eventDto = addHistory(eventDto, "Product validation successful");

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

        eventDto = addHistory(eventDto, "Product validation failed: " + message);

        return eventDto;
    }

    public void rollbackEvent(EventDto eventDto) {
        try {
            changeValidationToFail(eventDto);
            log.info("Rollback completed for orderId: {}", eventDto.payload().orderId());
        } catch (Exception e) {
            log.error("Error changing validation to fail, but continuing rollback: {}", e.getMessage());
        }

        var eventToRollback = eventDto.toBuilder()
                .status(FAIL)
                .source(CURRENT_SOURCE)
                .build();

        eventToRollback = addHistory(eventToRollback, "Rollback executed for event: " + eventDto.eventId() + " on product validation");

        kafkaProducer.sendEvent(jsonUtil.toJson(eventToRollback));
    }

    private void changeValidationToFail(EventDto eventDto) {
        if (isEmpty(eventDto.payload())) {
            log.warn("Payload is empty for rollback event: {}", eventDto.eventId());
            return;
        }

        validationRepository.findByOrderIdAndTransactionId(
                        eventDto.payload().orderId(),
                        eventDto.payload().transactionId()
                )
                .ifPresentOrElse(
                        validation -> {
                            validation.setSuccess(false);
                            validationRepository.save(validation);
                            log.info("Validation rolled back for orderId: {} and transactionId: {}",
                                    eventDto.payload().orderId(), eventDto.payload().transactionId());
                        },
                        () -> {
                            createValidation(eventDto, false);
                            log.info("Validation not found, created new failed validation for orderId: {} and transactionId: {}",
                                    eventDto.payload().orderId(), eventDto.payload().transactionId());
                        }
                );
    }

}
