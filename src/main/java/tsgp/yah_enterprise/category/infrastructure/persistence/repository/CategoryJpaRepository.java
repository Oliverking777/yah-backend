package tsgp.yah_enterprise.category.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.category.infrastructure.persistence.entity.CategoryJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    Optional<CategoryJpaEntity> findByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    Page<CategoryJpaEntity> findAllByOrderByCategoryNameAsc(Pageable pageable);
}