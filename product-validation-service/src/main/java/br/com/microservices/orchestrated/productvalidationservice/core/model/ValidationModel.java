package br.com.microservices.orchestrated.productvalidationservice.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "validation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "validation_id", nullable = false, updatable = false)
    private Integer validationId;

    @Column(name = "orderId", nullable = false, unique = true)
    private String orderId;

    @Column(name = "transactionId", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "createAt", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "updateAt", nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    public void prePersist() {
        var now = LocalDateTime.now(ZoneId.of("UTC"));
        createAt = now;
        updateAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateAt = LocalDateTime.now(ZoneId.of("UTC"));
    }

}
