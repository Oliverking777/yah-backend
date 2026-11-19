package tsgp.yah_enterprise.audit.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tsgp.yah_enterprise.audit.infrastructure.persistence.entity.AuditEntryJpaEntity;

import java.util.UUID;

public interface AuditEntryJpaRepository
        extends JpaRepository<AuditEntryJpaEntity, UUID>, JpaSpecificationExecutor<AuditEntryJpaEntity> {
}
