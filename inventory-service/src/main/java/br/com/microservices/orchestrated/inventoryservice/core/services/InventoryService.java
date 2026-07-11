package br.com.microservices.orchestrated.inventoryservice.core.services;

import br.com.microservices.orchestrated.inventoryservice.configs.exceptions.InsufficientInventoryException;
import br.com.microservices.orchestrated.inventoryservice.configs.exceptions.NotFoundException;
import br.com.microservices.orchestrated.inventoryservice.configs.exceptions.OrderAlreadyExistsException;
import br.com.microservices.orchestrated.inventoryservice.core.dtos.EventDto;
import br.com.microservices.orchestrated.inventoryservice.core.dtos.HistoryDto;
import br.com.microservices.orchestrated.inventoryservice.core.dtos.OrderDto;
import br.com.microservices.orchestrated.inventoryservice.core.dtos.OrderProductsDto;
import br.com.microservices.orchestrated.inventoryservice.core.model.InventoryModel;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventoryModel;
import br.com.microservices.orchestrated.inventoryservice.core.producers.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repositories.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repositories.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.microservices.orchestrated.inventoryservice.core.enums.SagaStatus.SUCCESS;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final InventoryRepository inventoryRepository;
    private final OrderInventoryRepository orderInventoryRepository;

    public void updateInventory(EventDto eventDto) {
        try {
            log.info("Updating inventory for event: {}", eventDto);
            checkCurrentValidation(eventDto);
            createOrderInventory(eventDto);
            updateInventory(eventDto.payload());
            eventDto = handleSuccess(eventDto);
        } catch (Exception ex) {
            log.error("Error while updating inventory ", ex);
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(eventDto));
    }

    private void checkCurrentValidation(EventDto eventDto) {

        if (orderInventoryRepository.existsByOrderIdAndTransactionId(eventDto.orderId(), eventDto.transactionId())) {
            log.info("Order already processed for event: {}", eventDto);
            throw new OrderAlreadyExistsException("Order already processed for orderId: " + eventDto.orderId() + " and transactionId: " + eventDto.transactionId());
        }

    }

    private void createOrderInventory(EventDto eventDto) {
        log.info("Creating order inventory for event: {}", eventDto);
        eventDto.payload().products().forEach(product -> {
            var inventory = findInventoryByProductCode(product.product().code());
            var orderInventory = createOrderInventory(eventDto, product, inventory);
            orderInventoryRepository.save(orderInventory);
        });
    }

    public InventoryModel findInventoryByProductCode(String productCode) {
        log.info("Finding inventory for product code {}", productCode);
        return inventoryRepository
                .findByProductCode(productCode)
                .orElseThrow(() -> new NotFoundException("Inventory not found for product code: " + productCode));
    }

    public OrderInventoryModel createOrderInventory(EventDto eventDto, OrderProductsDto product, InventoryModel inventory) {
        return OrderInventoryModel.builder()
                .inventory(inventory)
                .orderId(eventDto.payload().orderId())
                .transactionId(eventDto.transactionId())
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(product.quantity())
                .newQuantity(inventory.getAvailable() - product.quantity())
                .build();
    }

    private void updateInventory(OrderDto payload) {
        log.info("Updating inventory for order: {}", payload);
        payload.products().forEach(product -> {
            var inventory = findInventoryByProductCode(product.product().code());
            checkInventory(inventory.getAvailable(), product.quantity());
            inventory.setAvailable(inventory.getAvailable() - product.quantity());
            inventoryRepository.save(inventory);
        });
    }

    public void checkInventory(int available, int quantity) {
        log.info("Checking inventory: available={}, quantity={}", available, quantity);
        if (available < quantity) {
            log.error("Insufficient inventory: available={}, quantity={}", available, quantity);
            throw new InsufficientInventoryException("Insufficient inventory: available=" + available + ", quantity=" + quantity);
        }
    }

    private EventDto handleSuccess(EventDto eventDto) {
        eventDto = eventDto.toBuilder()
                .status(SUCCESS)
                .source(CURRENT_SOURCE)
                .build();

        eventDto = addHistory(eventDto, "Inventory updated successfully!");

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
