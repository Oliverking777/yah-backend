package tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import tsgp.yah_enterprise.purchasing.domain.model.PurchaseStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter @Setter @NoArgsConstructor
public class PurchaseJpaEntity {

    @Id
    @Column(name = "purchase_id", nullable = false, updatable = false)
    private UUID purchaseId;

    @Column(name = "purchase_number", nullable = false, updatable = false, length = 20)
    private String purchaseNumber;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "destination_location_id", nullable = false)
    private UUID destinationLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_status", nullable = false, length = 30)
    private PurchaseStatus purchaseStatus;

    /** Set at confirmation: goods were rejected or are missing on at least one line. */
    @Column(name = "has_discrepancy", nullable = false)
    private boolean discrepancy;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "note", length = 500)
    private String note;

    /** The supplier's delivery note number, recorded at confirmation. */
    @Column(name = "delivery_note", length = 50)
    private String deliveryNote;

    /** Who ordered. */
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<PurchaseItemJpaEntity> items = new ArrayList<>();

    /** When the goods were ordered. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Bumped on every save. The row lock (find for update) is what keeps two confirmations apart. */
    @Column(name = "version", nullable = false)
    private long version;
}