package tsgp.yah_enterprise.inventory.infrastructure.persistence.entity;

import tsgp.yah_enterprise.inventory.domain.model.StockInitializationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_initializations")
@Getter @Setter @NoArgsConstructor
public class StockInitializationJpaEntity {

    @Id
    @Column(name = "initialization_id", nullable = false, updatable = false)
    private UUID initializationId;

    @Column(name = "initialization_number", nullable = false, updatable = false, length = 20)
    private String initializationNumber;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockInitializationStatus status;

    @Column(name = "initialized_by", nullable = false, updatable = false)
    private UUID initializedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "initialization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @BatchSize(size = 50)
    private List<StockInitializationItemJpaEntity> items = new ArrayList<>();
}
