package br.com.microservices.orchestrated.productvalidationservice.core.repositories;

import br.com.microservices.orchestrated.productvalidationservice.core.model.ProductModel;
import br.com.microservices.orchestrated.productvalidationservice.core.model.ValidationModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<ValidationModel, Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    Optional<ValidationModel> findByOrderIdAndTransactionId(String orderId, String transactionId);
    
}
