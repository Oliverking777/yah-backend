package tsgp.yah_enterprise.sales.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import tsgp.yah_enterprise.sales.domain.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "receipts")
@Getter @Setter @NoArgsConstructor
public class ReceiptJpaEntity {

    @Id
    @Column(name = "receipt_id", nullable = false, updatable = false)
    private UUID receiptId;

    @Column(name = "receipt_number", nullable = false, updatable = false, length = 20)
    private String receiptNumber;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "issued_by", nullable = false, updatable = false)
    private UUID issuedBy;
}
