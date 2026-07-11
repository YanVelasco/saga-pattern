package br.com.microservices.orchestrated.inventoryservice.core.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "order_inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderInventoryModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_inventory_id", nullable = false, updatable = false)
    private Integer orderInventoryId;

    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryModel inventory;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "old_quantity", nullable = false)
    private Integer oldQuantity;

    @Column(name = "createAt", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

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
