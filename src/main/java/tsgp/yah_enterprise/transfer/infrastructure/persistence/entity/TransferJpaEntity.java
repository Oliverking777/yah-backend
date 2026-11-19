package tsgp.yah_enterprise.transfer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter @Setter @NoArgsConstructor
public class TransferJpaEntity {

    @Id
    @Column(name = "transfer_id", nullable = false, updatable = false)
    private UUID transferId;

    @Column(name = "transfer_number", nullable = false, updatable = false, length = 20)
    private String transferNumber;

    @Column(name = "source_location_id", nullable = false, updatable = false)
    private UUID sourceLocationId;

    @Column(name = "destination_location_id", nullable = false, updatable = false)
    private UUID destinationLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false, length = 30)
    private TransferStatus transferStatus;

    /** Set at receipt: true when at least one line arrived damaged or short. */
    @Column(name = "has_discrepancy", nullable = false)
    private boolean discrepancy;

    @Column(name = "note", length = 500, updatable = false)
    private String note;

    /** The sender: creating a transfer sends it. */
    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "received_by")
    private UUID receivedBy;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @BatchSize(size = 50) // avoids one extra query per transfer when listing
    private List<TransferItemJpaEntity> items = new ArrayList<>();

    /** When the goods were sent. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}