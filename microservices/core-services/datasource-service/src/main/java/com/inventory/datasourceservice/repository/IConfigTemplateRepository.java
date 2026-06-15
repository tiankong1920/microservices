package com.inventory.datasourceservice.repository;

import com.inventory.datasourceservice.entity.ConfigTemplate;
import com.inventory.datasourceservice.entity.DatasourceConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface IConfigTemplateRepository extends JpaRepository<ConfigTemplate, Long> {

    Page<ConfigTemplate> findByTenantId(String tenantId, Pageable pageable);

    Optional<ConfigTemplate> findByIdAndTenantId(Long id, String tenantId);

    List<ConfigTemplate> findByTenantIdAndType(String tenantId, DatasourceConfig.DatasourceType type);

    @Query("SELECT t FROM ConfigTemplate t WHERE (t.tenantId = :tenantId OR t.isPublic = true) " +
           "AND (:type IS NULL OR t.type = :type)")
    Page<ConfigTemplate> findAvailableTemplates(@Param("tenantId") String tenantId,
                                                 @Param("type") DatasourceConfig.DatasourceType type,
                                                 Pageable pageable);

    boolean existsByNameAndTenantId(String name, String tenantId);

    @Transactional
    void deleteByIdAndTenantId(Long id, String tenantId);
}
