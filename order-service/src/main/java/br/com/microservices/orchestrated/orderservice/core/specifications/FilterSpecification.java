package br.com.microservices.orchestrated.orderservice.core.specifications;

import br.com.microservices.orchestrated.orderservice.core.dtos.FiltersDto;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class FilterSpecification {

    private FilterSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Query eventFilters(FiltersDto filter) {
        Query query = new Query();

        if (filter != null) {
            List<Criteria> criteriaList = new ArrayList<>();

            addEqualCriteria(criteriaList, "orderId", filter.orderId());
            addEqualCriteria(criteriaList, "transactionId", filter.transactionId());

            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        return query;
    }

    private static void addEqualCriteria(List<Criteria> criteriaList, String field, String value) {
        if (hasText(value)) {
            criteriaList.add(Criteria.where(field).is(value));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
