package tsgp.yah_enterprise.category.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.category.infrastructure.persistence.entity.CategoryJpaEntity;
import tsgp.yah_enterprise.category.domain.model.Category;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        return Category.reconstitute(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getCategoryStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public CategoryJpaEntity toEntity(Category category) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setCategoryId(category.getCategoryId());
        entity.setCategoryName(category.getCategoryName().value());
        entity.setDescription(category.getDescription());
        entity.setCategoryStatus(category.getCategoryStatus());
        entity.setCreatedAt(category.getCreatedAt());
        entity.setUpdatedAt(category.getUpdatedAt());
        return entity;
    }
}