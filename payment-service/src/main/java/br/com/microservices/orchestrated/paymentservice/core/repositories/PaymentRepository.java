package br.com.microservices.orchestrated.paymentservice.core.repositories;

import br.com.microservices.orchestrated.paymentservice.core.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentModel, Integer> {

    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    Optional<PaymentModel> findByOrderIdAndTransactionId(String orderId, String transactionId);

}