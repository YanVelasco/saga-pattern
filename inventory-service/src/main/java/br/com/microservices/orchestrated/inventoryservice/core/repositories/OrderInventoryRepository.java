package br.com.microservices.orchestrated.inventoryservice.core.repositories;

import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderInventoryRepository extends JpaRepository<OrderInventoryModel, Integer> {
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    List<OrderInventoryModel> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
