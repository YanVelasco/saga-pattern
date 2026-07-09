package br.com.microservices.orchestrated.paymentservice.core.model;

import br.com.microservices.orchestrated.paymentservice.core.dtos.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Integer paymentId;

    @Column(name = "orderId", nullable = false, unique = true)
    private String orderId;

    @Column(name = "transactionId", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "createAt", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "updateAt", nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now(ZoneId.of("UTC"));
        createAt = now;
        updateAt = now;
        paymentStatus = PaymentStatus.PENDING;
    }

    @PreUpdate
    public void preUpdate() {
        updateAt = LocalDateTime.now(ZoneId.of("UTC"));
    }

}
