package br.com.microservices.orchestrated.productvalidationservice.core.repositories;

import br.com.microservices.orchestrated.productvalidationservice.core.model.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductModel, Integer> {

    boolean existsByCode(String code);

}
