package tsgp.yah_enterprise.category.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.category.infrastructure.persistence.mapper.CategoryMapper;
import tsgp.yah_enterprise.category.infrastructure.persistence.repository.CategoryJpaRepository;
import tsgp.yah_enterprise.category.domain.exception.CategoryNameAlreadyExistsException;
import tsgp.yah_enterprise.category.domain.model.Category;
import tsgp.yah_enterprise.category.domain.repository.CategoryRepository;
import tsgp.yah_enterprise.category.domain.valueobject.CategoryName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;
    private final CategoryMapper mapper;

    @Override
    public Category save(Category category) {
        try {
            // saveAndFlush so a concurrent duplicate name fails here, not later at commit time
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(category)));
        } catch (DataIntegrityViolationException e) {
            throw new CategoryNameAlreadyExistsException(category.getCategoryName());
        }
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        return jpaRepository.findById(categoryId).map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(CategoryName categoryName) {
        return jpaRepository.findByCategoryNameIgnoreCase(categoryName.value()).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll(int page, int size) {
        return jpaRepository.findAllByOrderByCategoryNameAsc(PageRequest.of(page, size))
                .getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public boolean existsById(UUID categoryId) {
        return jpaRepository.existsById(categoryId);
    }

    @Override
    public boolean existsByName(CategoryName categoryName) {
        return jpaRepository.existsByCategoryNameIgnoreCase(categoryName.value());
    }
}