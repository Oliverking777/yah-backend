package tsgp.yah_enterprise.location.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.location.domain.model.LocationStatus;
import tsgp.yah_enterprise.location.domain.model.LocationType;

import java.time.Instant;
import java.util.UUID;

/** Persistence representation of a Location. Never leaves the infrastructure layer. */
@Entity
@Table(
        name = "locations",
        uniqueConstraints = @UniqueConstraint(name = "uq_locations_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class LocationJpaEntity {

    @Id
    @Column(name = "location_id", nullable = false, updatable = false)
    private UUID locationId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, updatable = false, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    @Column(name = "address", length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_status", nullable = false, length = 20)
    private LocationStatus locationStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}