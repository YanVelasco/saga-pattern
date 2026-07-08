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

import static br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus.FAIL;
import static br.com.microservices.orchestrated.productvalidationservice.core.enums.SagaStatus.SUCCESS;
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
            createValidation(eventDto);
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
            validadeExistingProducts(product.code());
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

    public void validateProductInformed(ProductDto productDto) {
        if (isEmpty(productDto) || isEmpty(productDto.code())) {
            throw new ProductOrCodeIsMissing("Product or product code is empty");
        }
    }

    public void validadeExistingProducts(String code) {
        if (!productRepository.existsByCode(code)) {
            throw new NotExistsException("Product with code " + code + " does not exist");
        }
    }

    private void createValidation(EventDto eventDto) {
        var validation = ValidationModel.builder()
                .orderId(eventDto.payload().orderId())
                .transactionId(eventDto.transactionId())
                .success(true)
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

    private EventDto handleFailCurrentNotExecuted(EventDto eventDto, String message) {
        eventDto = eventDto.toBuilder()
                .status(FAIL)
                .source(CURRENT_SOURCE)
                .build();

        eventDto = addHistory(eventDto, "Product validation failed: " + message);
        
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

}
