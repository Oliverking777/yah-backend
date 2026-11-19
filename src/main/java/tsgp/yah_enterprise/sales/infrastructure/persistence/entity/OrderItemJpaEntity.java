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
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor
public class OrderItemJpaEntity {

    @Id
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "conversion_factor", nullable = false)
    private int conversionFactor;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "price_id")
    private UUID priceId;

    @Column(name = "discount_amount", nullable = false)
    private long discountAmount;

    @Column(name = "line_total", nullable = false)
    private long lineTotal;
}
