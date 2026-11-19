package tsgp.yah_enterprise.location.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of a user-to-location assignment.
 * user_id is a plain column (no FK to the IAM table) so the two modules stay decoupled;
 * location_id is also a plain column, with its FK declared in the database migration.
 */
@Entity
@Table(
        name = "location_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uq_location_assignments_user", columnNames = "user_id"),
        indexes = @Index(name = "idx_location_assignments_location_id", columnList = "location_id"))
@Getter
@Setter
@NoArgsConstructor
public class LocationAssignmentJpaEntity {

    @Id
    @Column(name = "assignment_id", nullable = false, updatable = false)
    private UUID assignmentId;

    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
}