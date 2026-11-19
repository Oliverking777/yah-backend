package tsgp.yah_enterprise.unit.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.unit.domain.model.UnitStatus;

/**
 * Persistence representation of a Unit. Never leaves the infrastructure layer.
 * Name uniqueness ignoring case is enforced by a unique index on LOWER(unit_name) created in the
 * migration (JPA cannot declare an expression index). Code uniqueness is a plain unique constraint.
 */
@Entity
@Table(name = "units")
@Getter
@Setter
@NoArgsConstructor
public class UnitJpaEntity {

    @Id
    @Column(name = "unit_id", nullable = false, updatable = false)
    private UUID unitId;

    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;

    @Column(name = "unit_code", nullable = false, updatable = false, length = 10)
    private String unitCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_status", nullable = false, length = 20)
    private UnitStatus unitStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
