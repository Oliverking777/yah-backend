package tsgp.yah_enterprise.category.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.category.domain.model.CategoryStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of a Category. Never leaves the infrastructure layer.
 * Name uniqueness ignoring case is enforced by a unique index on LOWER(category_name)
 * created in the database migration (JPA cannot declare an expression index).
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryJpaEntity {

    @Id
    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_status", nullable = false, length = 20)
    private CategoryStatus categoryStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}